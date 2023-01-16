package net.azisaba.simpleproxy.proxy.connection;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.haproxy.HAProxyCommand;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion;
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol;
import net.azisaba.simpleproxy.api.config.ServerInfo;
import net.azisaba.simpleproxy.proxy.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.Inet6Address;
import java.net.InetSocketAddress;

public class UDPMessageForwarderForwarder extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger LOGGER = LogManager.getLogger();

    protected final UDPMessageForwarder forwarder;
    protected final ServerInfo serverInfo;
    protected final InetSocketAddress sourceAddress;

    public UDPMessageForwarderForwarder(UDPMessageForwarder forwarder, @NotNull ServerInfo serverInfo, @NotNull InetSocketAddress sourceAddress) {
        this.forwarder = forwarder;
        this.serverInfo = serverInfo;
        this.sourceAddress = sourceAddress;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (serverInfo.isProxyProtocol()) {
            initHAProxy(ctx);
        }
        ctx.read();
        forwarder.remoteActive(ctx.channel());
        LOGGER.info("Remote: Established connection: " + ctx.channel());
    }

    public void initHAProxy(@NotNull ChannelHandlerContext ctx) {
        HAProxyProxiedProtocol proxiedProtocol;
        String hostAddress = sourceAddress.getAddress().getHostAddress();
        int port = sourceAddress.getPort();
        if (sourceAddress.getAddress() instanceof Inet6Address) {
            proxiedProtocol = HAProxyProxiedProtocol.UDP6;
        } else {
            proxiedProtocol = HAProxyProxiedProtocol.UDP4;
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
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) /*throws Exception*/ {
        if (forwarder.deactivated || !ctx.channel().isActive()) {
            ctx.channel().close();
            return;
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
