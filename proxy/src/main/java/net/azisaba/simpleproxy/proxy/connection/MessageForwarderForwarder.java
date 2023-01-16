package net.azisaba.simpleproxy.proxy.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.haproxy.HAProxyCommand;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion;
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol;
import net.azisaba.simpleproxy.api.config.ServerInfo;
import net.azisaba.simpleproxy.proxy.config.ProxyConfigInstance;
import net.azisaba.simpleproxy.proxy.util.MemoryReserve;
import net.azisaba.simpleproxy.proxy.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class MessageForwarderForwarder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();

    protected final MessageForwarder forwarder;
    protected final ServerInfo serverInfo;

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
        if (ProxyConfigInstance.debug) {
            LOGGER.info("Remote: Established connection: {} (forwarder: A: {} Ch: {} CA: {} D: {})",
                    ctx.channel(), forwarder.active, forwarder.sourceAddress, forwarder.channel.isActive(), forwarder.deactivated);
        }
        if (!forwarder.channel.isActive()) {
            if (ProxyConfigInstance.debug) {
                LOGGER.info("Remote: Closing connection due to inactive forwarder channel");
            }
            ctx.close();
            forwarder.deactivate();
        }
    }

    public void initHAProxy(@NotNull ChannelHandlerContext ctx) {
        HAProxyProxiedProtocol proxiedProtocol = HAProxyProxiedProtocol.UNKNOWN;
        SocketAddress socketAddress = forwarder.sourceAddress;
        String hostAddress = null;
        int port = 0;
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            hostAddress = inetSocketAddress.getAddress().getHostAddress();
            port = inetSocketAddress.getPort();
            if (inetSocketAddress.getAddress() instanceof Inet6Address) {
                proxiedProtocol = HAProxyProxiedProtocol.TCP6;
            } else {
                proxiedProtocol = HAProxyProxiedProtocol.TCP4;
            }
        } else if (socketAddress instanceof DomainSocketAddress) {
            hostAddress = ((DomainSocketAddress) socketAddress).path();
            proxiedProtocol = HAProxyProxiedProtocol.UNIX_STREAM;
        } else {
            LOGGER.warn("Unrecognized socket address type {}: {}", socketAddress.getClass().getTypeName(), socketAddress);
        }
        ctx.channel().writeAndFlush(new HAProxyMessage(
                HAProxyProtocolVersion.V2,
                HAProxyCommand.PROXY,
                proxiedProtocol,
                hostAddress,
                Util.getDestinationAddressForHAProxy(proxiedProtocol, serverInfo.getHost()),
                port,
                serverInfo.getPort()
        ));
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (forwarder.deactivated || !ctx.channel().isActive()) {
            ctx.channel().close();
            return;
        }
        if (ProxyConfigInstance.debug && msg instanceof ByteBuf) {
            LOGGER.debug("< IN: " + ((ByteBuf) msg).readableBytes());
        }
        Object write = msg;
        if (write instanceof ByteBuf) {
            write = ((ByteBuf) write).copy();
        }
        forwarder.channel.writeAndFlush(write).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                ctx.channel().read();
            } else {
                future.channel().close();
            }
        });
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        forwarder.deactivate();
        ctx.channel().close();
        if (ProxyConfigInstance.debug) {
            LOGGER.info("Remote: Closed connection: {} (forwarder: A: {} Ch: {} CA: {} D: {})",
                    ctx.channel(), forwarder.active, forwarder.sourceAddress, forwarder.channel.isActive(), forwarder.deactivated);
        }
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Caught exception!", cause);
        ctx.channel().close();
        if (cause instanceof OutOfMemoryError) {
            MemoryReserve.tryShutdownGracefully();
        }
    }
}
