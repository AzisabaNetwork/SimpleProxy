package net.azisaba.simpleProxy.proxy.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleProxy.api.config.ListenerInfo;
import net.azisaba.simpleProxy.api.config.ServerInfo;
import net.azisaba.simpleProxy.proxy.ProxyInstance;
import net.azisaba.simpleProxy.proxy.config.ProxyConfigInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class MessageForwarder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();

    public final ListenerInfo listenerInfo;
    protected final Queue<Object> queue = new ArrayDeque<>();
    protected Channel channel;
    protected Channel remote = null;
    protected boolean remoteConnecting = false;
    boolean deactivated = false;
    boolean isRemoteActive = false;

    public MessageForwarder(Channel channel, ListenerInfo listenerInfo) {
        this.channel = channel;
        this.listenerInfo = listenerInfo;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.read();
        if (ProxyConfigInstance.debug) {
            LOGGER.info("Forwarder: Established connection: " + ctx.channel());
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        deactivated = true;
        ctx.channel().close();
        if (remote != null) remote.close();
        if (ProxyConfigInstance.debug) {
            LOGGER.info("Forwarder: Closed connection: " + ctx.channel());
        }
    }

    public void writeToRemote(Object msg) {
        if (ProxyConfigInstance.debug && msg instanceof ByteBuf) {
            LOGGER.debug("> OUT: " + ((ByteBuf) msg).readableBytes());
        }
        remote.writeAndFlush(msg);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (deactivated || !channel.isActive()) {
            ctx.channel().close();
            super.channelRead(ctx, msg);
            return;
        }
        if (remote == null && !remoteConnecting) {
            remoteConnecting = true;
            List<ServerInfo> list = new ArrayList<>(listenerInfo.getServers());
            Collections.shuffle(list);
            ChannelFuture future = ProxyInstance.getInstance()
                    .getConnectionListener()
                    .connect(this, list.get(0));
            remote = future.channel();
        }
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
    }

    void remoteActive() {
        isRemoteActive = true;
        flushQueue();
    }

    public void flushQueue() {
        Object msg;
        while ((msg = queue.poll()) != null) {
            writeToRemote(msg);
        }
    }
}
