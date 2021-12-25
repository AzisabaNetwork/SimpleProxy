package net.azisaba.simpleProxy.proxy.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleProxy.proxy.ProxyInstance;
import net.azisaba.simpleProxy.proxy.config.ServerInfo;
import net.azisaba.simpleProxy.proxy.config.ListenerInfo;
import net.azisaba.simpleProxy.proxy.config.ProxyConfig;
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

    private final ListenerInfo listenerInfo;
    private final Queue<Object> queue = new ArrayDeque<>();
    final Channel channel;
    private Channel remote = null;
    boolean deactivated = false;
    boolean isRemoteActive = false;

    public MessageForwarder(Channel channel, ListenerInfo listenerInfo) {
        this.channel = channel;
        this.listenerInfo = listenerInfo;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        List<ServerInfo> list = new ArrayList<>(listenerInfo.getServers());
        Collections.shuffle(list);
        ChannelFuture future = ProxyInstance.getInstance()
                .getConnectionListener()
                .connect(this, list.get(0));
        ctx.read();
        remote = future.channel();
        LOGGER.info("Forwarder: Established connection: " + ctx.channel());
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        deactivated = true;
        ctx.channel().close();
        LOGGER.info("Forwarder: Closed connection: " + ctx.channel());
    }

    public void writeToRemote(Object msg) {
        if (ProxyConfig.debug && msg instanceof ByteBuf) {
            LOGGER.debug("> OUT: " + ((ByteBuf) msg).readableBytes());
        }
        remote.writeAndFlush(msg);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (deactivated || !channel.isActive()) {
            ctx.channel().close();
            return;
        }
        if (!remote.isActive()) {
            queue.add(msg);
            return;
        }
        flushQueue();
        writeToRemote(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Caught exception!", cause);
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
