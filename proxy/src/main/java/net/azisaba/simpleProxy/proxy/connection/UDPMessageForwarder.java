package net.azisaba.simpleProxy.proxy.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.azisaba.simpleProxy.api.config.ListenerInfo;
import net.azisaba.simpleProxy.api.config.Protocol;
import net.azisaba.simpleProxy.api.config.ServerInfo;
import net.azisaba.simpleProxy.proxy.ProxyInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class UDPMessageForwarder extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger LOGGER = LogManager.getLogger();

    public final ListenerInfo listenerInfo;
    protected final Queue<DatagramPacket> queue = new ArrayDeque<>();
    protected Channel channel;
    protected Map<InetSocketAddress, Channel> remotes = new Object2ObjectOpenHashMap<>();
    protected Map<InetSocketAddress, Long> lastPacketReceived = new Object2ObjectOpenHashMap<>();
    boolean deactivated = false;
    boolean isRemoteActive = false;

    public UDPMessageForwarder(Channel channel, ListenerInfo listenerInfo) {
        if (listenerInfo.getProtocol() == Protocol.TCP) throw new RuntimeException("Not expecting TCP protocol type");
        this.channel = channel;
        this.listenerInfo = listenerInfo;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        channel = ctx.channel();
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LOGGER.info("Forwarder: Became active: {}", ctx.channel());
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        deactivated = true;
        ctx.channel().close();
        for (InetSocketAddress address : remotes.keySet()) {
            dispose(address);
        }
        LOGGER.info("Forwarder: Became inactive: {}", ctx.channel());
    }

    public void writeToRemote(@NotNull Channel ch, DatagramPacket msg) {
        ch.write(msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        if (deactivated || !channel.isActive()) {
            //ctx.channel().close();
            return;
        }
        Channel remote = getRemote(msg.sender());
        if (!remote.isActive()) {
            queue.add(msg);
            return;
        }
        flushQueue(remote);
        writeToRemote(remote, new DatagramPacket(msg.content(), (InetSocketAddress) remote.remoteAddress()));
    }

    @NotNull
    protected Channel getRemote(@NotNull InetSocketAddress address) {
        long currentTime = System.currentTimeMillis();
        long lastReceived = lastPacketReceived.computeIfAbsent(address, v -> currentTime);
        if (lastReceived + listenerInfo.getTimeout() < currentTime) {
            dispose(address);
        }
        Channel ch;
        if (!remotes.containsKey(address)) {
            ch = connectToRandomHost(address).channel();
            remotes.put(address, ch);
            connectionActive(address, ch);
        } else {
            ch = remotes.get(address);
        }
        lastPacketReceived.put(address, currentTime);
        return ch;
    }

    protected void dispose(@NotNull InetSocketAddress address) {
        lastPacketReceived.remove(address);
        Channel ch = remotes.remove(address);
        ch.close().syncUninterruptibly();
        LOGGER.info("Forwarder: Closed connection: {} from {}", ch, address);
    }

    @NotNull
    protected ChannelFuture connectToRandomHost(@NotNull InetSocketAddress address) {
        List<ServerInfo> list = new ArrayList<>(listenerInfo.getServers());
        Collections.shuffle(list);
        return ProxyInstance.getInstance()
                .getConnectionListener()
                .connect(this, address, list.get(0));
    }

    protected void connectionActive(@NotNull InetSocketAddress address, @NotNull Channel ch) {
        LOGGER.info("Forwarder: Connection active: {} from {}", ch, address);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Caught exception!", cause);
        ctx.channel().close();
    }

    void remoteActive(Channel ch) {
        isRemoteActive = true;
        flushQueue(ch);
    }

    public void flushQueue(Channel ch) {
        DatagramPacket msg;
        while ((msg = queue.poll()) != null) {
            writeToRemote(ch, new DatagramPacket(msg.content(), (InetSocketAddress) ch.remoteAddress()));
        }
    }
}
