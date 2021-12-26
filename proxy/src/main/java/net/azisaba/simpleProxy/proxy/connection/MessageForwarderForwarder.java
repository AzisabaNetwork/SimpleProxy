package net.azisaba.simpleProxy.proxy.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.haproxy.HAProxyCommand;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion;
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol;
import net.azisaba.simpleProxy.proxy.config.ServerInfo;
import net.azisaba.simpleProxy.proxy.config.ProxyConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class MessageForwarderForwarder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();

    private final MessageForwarder forwarder;
    private final ServerInfo serverInfo;

    public MessageForwarderForwarder(MessageForwarder forwarder, @NotNull ServerInfo serverInfo) {
        this.forwarder = forwarder;
        this.serverInfo = serverInfo;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (serverInfo.isProxyProtocol()) {
            initHAProxy(ctx);
        }
        ctx.read();
        forwarder.remoteActive();
        LOGGER.info("Remote: Established connection: " + ctx.channel());
    }

    public void initHAProxy(@NotNull ChannelHandlerContext ctx) throws UnknownHostException {
        HAProxyProxiedProtocol proxiedProtocol;
        if (InetAddress.getByName(serverInfo.getHost()) instanceof Inet6Address) {
            proxiedProtocol = HAProxyProxiedProtocol.TCP6;
        } else {
            proxiedProtocol = HAProxyProxiedProtocol.TCP4;
        }
        SocketAddress socketAddress = forwarder.channel.remoteAddress();
        String hostAddress = null;
        int port = 0;
        if (socketAddress instanceof InetSocketAddress) {
            hostAddress = ((InetSocketAddress) socketAddress).getAddress().getHostAddress();
            port = ((InetSocketAddress) socketAddress).getPort();
        } else if (socketAddress instanceof DomainSocketAddress) {
            hostAddress = ((DomainSocketAddress) socketAddress).path();
        } else {
            LOGGER.warn("Unrecognized socket address type {}: {}", socketAddress.getClass().getTypeName(), socketAddress);
        }
        ctx.channel().writeAndFlush(new HAProxyMessage(
                HAProxyProtocolVersion.V2,
                HAProxyCommand.PROXY,
                proxiedProtocol,
                hostAddress,
                serverInfo.getHost(),
                port,
                serverInfo.getPort()
        ));
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (forwarder.deactivated || !ctx.channel().isActive()) {
            ctx.channel().close();
            return;
        }
        if (ProxyConfig.debug && msg instanceof ByteBuf) {
            LOGGER.debug("< IN: " + ((ByteBuf) msg).readableBytes());
        }
        forwarder.channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                ctx.channel().read();
            } else {
                future.channel().close();
            }
        });
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        forwarder.deactivated = true;
        ctx.channel().close();
        LOGGER.info("Remote: Closed connection: " + ctx.channel());
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Caught exception!", cause);
        ctx.channel().close();
    }
}
