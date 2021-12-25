package net.azisaba.simpleProxy.proxy.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.codec.haproxy.HAProxyMessageEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.azisaba.simpleProxy.api.event.connection.ConnectionInitEvent;
import net.azisaba.simpleProxy.api.event.connection.RemoteConnectionInitEvent;
import net.azisaba.simpleProxy.proxy.config.ServerInfo;
import net.azisaba.simpleProxy.proxy.config.ListenerInfo;
import net.azisaba.simpleProxy.proxy.config.ProxyConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicLong BOSS_THREAD_COUNT = new AtomicLong();
    private static final AtomicLong WORKER_THREAD_COUNT = new AtomicLong();
    private static final AtomicLong CLIENT_WORKER_THREAD_COUNT = new AtomicLong();
    private final Class<? extends ServerSocketChannel> serverSocketChannelType;
    private final Class<? extends SocketChannel> socketChannelType;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final EventLoopGroup clientWorkerGroup;
    private final List<ChannelFuture> futures = new ArrayList<>();

    public ConnectionListener() {
        if (ProxyConfig.isEpoll()) {
            bossGroup = new EpollEventLoopGroup(r -> {
                Thread t = new Thread(r);
                t.setName("Netty Epoll IO Server Boss Thread #" + BOSS_THREAD_COUNT.incrementAndGet());
                return t;
            });
            workerGroup = new EpollEventLoopGroup(r -> {
                Thread t = new Thread(r);
                t.setName("Netty Epoll IO Server Worker Thread #" + WORKER_THREAD_COUNT.incrementAndGet());
                return t;
            });
            clientWorkerGroup = new EpollEventLoopGroup(r -> {
                Thread t = new Thread(r);
                t.setName("Netty Epoll IO Client Worker Thread #" + CLIENT_WORKER_THREAD_COUNT.incrementAndGet());
                return t;
            });
            serverSocketChannelType = EpollServerSocketChannel.class;
            socketChannelType = EpollSocketChannel.class;
            LOGGER.info("Using epoll channel type");
        } else {
            bossGroup = new NioEventLoopGroup(r -> {
                Thread t = new Thread(r);
                t.setName("Netty IO Boss Server Thread #" + BOSS_THREAD_COUNT.incrementAndGet());
                return t;
            });
            workerGroup = new NioEventLoopGroup(r -> {
                Thread t = new Thread(r);
                t.setName("Netty IO Worker Server Thread #" + WORKER_THREAD_COUNT.incrementAndGet());
                return t;
            });
            clientWorkerGroup = new NioEventLoopGroup(r -> {
                Thread t = new Thread(r);
                t.setName("Netty IO Client Worker Thread #" + CLIENT_WORKER_THREAD_COUNT.incrementAndGet());
                return t;
            });
            serverSocketChannelType = NioServerSocketChannel.class;
            socketChannelType = NioSocketChannel.class;
            LOGGER.info("Using normal channel type");
        }
    }

    public void listen(@NotNull ListenerInfo listenerInfo) {
        if (listenerInfo.getServers().isEmpty()) {
            LOGGER.warn("Listener for " + listenerInfo.getListenPort() + " has empty forwardTo list, skipping");
            return;
        }
        ChannelFuture future = new ServerBootstrap().group(bossGroup, workerGroup)
                .channel(serverSocketChannelType)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(@NotNull SocketChannel ch) {
                        try {
                            ch.config().setTcpNoDelay(true);
                        } catch (ChannelException ignore) {}
                        ch.pipeline()
                                .addFirst(new ReadTimeoutHandler(listenerInfo.getTimeout(), TimeUnit.MILLISECONDS))
                                .addFirst("rule_check_handler", RuleCheckHandler.INSTANCE);
                        if (listenerInfo.isProxyProtocol()) {
                            ch.pipeline().addLast("haproxy_message_decoder", new HAProxyMessageDecoder());
                        }
                        ch.pipeline().addLast("message_forwarder", new MessageForwarder(ch, listenerInfo));
                        new ConnectionInitEvent(ch).callEvent();
                    }
                })
                .bind(listenerInfo.getListenPort())
                .syncUninterruptibly();
        if (listenerInfo.isProxyProtocol()) {
            LOGGER.warn("Proxy protocol enabled for listener {}, please ensure this listener is properly firewalled.", future.channel().toString());
        }
        LOGGER.info("Listening on {}", future.channel().toString());
        futures.add(future);
    }

    @NotNull
    public ChannelFuture connect(@NotNull MessageForwarder forwarder, @NotNull ServerInfo serverInfo) {
        ChannelFuture future = new Bootstrap()
                .group(clientWorkerGroup)
                .channel(socketChannelType)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(@NotNull SocketChannel ch) {
                        try {
                            ch.config().setTcpNoDelay(true);
                        } catch (ChannelException ignore) {}
                        ch.pipeline().addLast("message_forwarder_forwarder", new MessageForwarderForwarder(forwarder, serverInfo));
                        if (serverInfo.isProxyProtocol()) {
                            ch.pipeline().addFirst("haproxy_message_encoder", HAProxyMessageEncoder.INSTANCE);
                        }
                        new RemoteConnectionInitEvent(ch).callEvent();
                    }
                })
                .connect(serverInfo.getHost(), serverInfo.getPort());
        futures.add(future);
        return future;
    }

    public void closeFutures() {
        for (ChannelFuture future : futures) {
            LOGGER.info("Closing listener: {}", future.channel().toString());
            future.channel().close().syncUninterruptibly();
        }
        futures.clear();
    }

    public void close() {
        closeFutures();
        clientWorkerGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
        bossGroup.shutdownGracefully().syncUninterruptibly();
    }
}
