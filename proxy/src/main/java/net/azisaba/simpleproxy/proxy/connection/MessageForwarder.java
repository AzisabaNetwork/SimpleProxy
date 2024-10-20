package net.azisaba.simpleproxy.proxy.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.azisaba.simpleproxy.api.config.ListenerInfo;
import net.azisaba.simpleproxy.api.config.ServerInfo;
import net.azisaba.simpleproxy.api.event.connection.RemoteConnectionActiveEvent;
import net.azisaba.simpleproxy.proxy.ProxyInstance;
import net.azisaba.simpleproxy.proxy.config.ProxyConfigInstance;
import net.azisaba.simpleproxy.proxy.util.MemoryReserve;
import net.azisaba.simpleproxy.proxy.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class MessageForwarder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();

    public final ListenerInfo listenerInfo;
    public final ServerInfo remoteServerInfo;
    protected final Queue<Object> queue = new ArrayDeque<>();
    protected Channel channel;
    protected Channel remote = null;
    protected boolean remoteConnecting = false;
    /**
     * The address which the user is connecting from.
     */
    protected SocketAddress sourceAddress;
    protected boolean active = false;
    protected boolean deactivated = false;
    protected boolean isRemoteActive = false;

    public MessageForwarder(@NotNull Channel channel, @NotNull ListenerInfo listenerInfo, @NotNull ServerInfo remoteServerInfo) {
        this.channel = channel;
        this.listenerInfo = listenerInfo;
        this.remoteServerInfo = remoteServerInfo;
        this.sourceAddress = channel.remoteAddress();
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.read();
        if (ProxyConfigInstance.debug) {
            LOGGER.info("Forwarder: Established connection: {}", ctx.channel());
        }
        active = true;
        if (listenerInfo.isConnectOnActive()) {
            connectToRemote();
        }
    }

    public void activate() {
        if (!active) {
            channel.read();
            if (ProxyConfigInstance.debug) {
                LOGGER.info("Forwarder: Established connection: {}", channel);
            }
        }
        if (remote == null && !remoteConnecting) {
            connectToRemote();
        }
    }

    private void connectToRemote() {
        if (channel.pipeline().context(ReadTimeoutHandler.class) != null) {
            channel.pipeline().remove(ReadTimeoutHandler.class);
            channel.pipeline().addFirst(new ReadTimeoutHandler(listenerInfo.getTimeout(), TimeUnit.MILLISECONDS));
        }
        remoteConnecting = true;
        if (ProxyConfigInstance.debug) {
            LOGGER.info("Forwarder: Connecting to remote server: {}", remoteServerInfo);
        }
        ChannelFuture future = ProxyInstance.getInstance()
                .getConnectionListener()
                .connect(this, remoteServerInfo);
        remote = future.channel();
    }

    public void deactivate() {
        if (deactivated) {
            return;
        }
        deactivated = true;
        channel.close();
        if (remote != null) remote.close();
        if (ProxyConfigInstance.debug) {
            int freed = Util.release(queue);
            LOGGER.info("Forwarder: Closed connection: {} (freed {} objects)", channel, freed);
        } else {
            Util.release(queue);
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        deactivate();
    }

    /**
     * Writes a message to the remote server.
     * @param msg the message
     * @throws NullPointerException if remote is null
     */
    public void writeToRemote(Object msg) throws NullPointerException {
        if (ProxyConfigInstance.debug && msg instanceof ByteBuf) {
            LOGGER.debug("> OUT: " + ((ByteBuf) msg).readableBytes());
        }
        remote.writeAndFlush(msg);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (msg instanceof HAProxyMessage) {
            String sourceAddress = ((HAProxyMessage) msg).sourceAddress();
            if (sourceAddress != null) {
                int port = ((HAProxyMessage) msg).sourcePort();
                this.sourceAddress = new InetSocketAddress(sourceAddress, port);
            }
        }
        if (deactivated || !channel.isActive()) {
            ctx.channel().close();
            super.channelRead(ctx, msg);
            return;
        }
        activate();
        if (remote == null || !remote.isActive()) {
            if (msg instanceof ByteBuf) {
                queue.add(((ByteBuf) msg).copy());
            } else {
                queue.add(msg);
            }
            super.channelRead(ctx, msg);
            return;
        }
        flushQueue();
        if (msg instanceof ByteBuf) {
            writeToRemote(((ByteBuf) msg).copy());
        } else {
            writeToRemote(msg);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Caught exception in {}!", ctx.channel(), cause);
        ctx.channel().close();
        if (cause instanceof OutOfMemoryError) {
            MemoryReserve.tryShutdownGracefully();
        }
    }

    void remoteActive() {
        isRemoteActive = true;
        flushQueue();
        new RemoteConnectionActiveEvent(listenerInfo, remote, channel, sourceAddress).callEvent();
    }

    public void flushQueue() {
        Object msg;
        while ((msg = queue.poll()) != null) {
            writeToRemote(msg);
        }
    }
}
