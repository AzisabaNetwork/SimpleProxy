package net.azisaba.simpleProxy.proxy.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.codec.haproxy.HAProxyMessageEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.azisaba.simpleProxy.api.config.ListenerInfo;
import net.azisaba.simpleProxy.api.config.Protocol;
import net.azisaba.simpleProxy.api.config.ServerInfo;
import net.azisaba.simpleProxy.api.event.connection.ConnectionInitEvent;
import net.azisaba.simpleProxy.api.event.connection.RemoteConnectionInitEvent;
import net.azisaba.simpleProxy.proxy.builtin.BuiltinTypeHandler;
import net.azisaba.simpleProxy.proxy.config.ProxyConfigInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private static final AtomicLong BOSS_THREAD_COUNT = new AtomicLong();
    private static final AtomicLong WORKER_THREAD_COUNT = new AtomicLong();
    private static final AtomicLong CLIENT_WORKER_THREAD_COUNT = new AtomicLong();
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final EventLoopGroup clientWorkerGroup;
    private final List<ChannelFuture> futures = new ArrayList<>();

    public ConnectionListener() {
        if (ProxyConfigInstance.isEpoll()) {
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
            LOGGER.info("Using epoll channel type");
        } else {
            bossGroup = new NioEventLoopGroup(r -> {
                Thread t = new Thread(r);
                t.setName("Netty IO Server Boss Thread #" + BOSS_THREAD_COUNT.incrementAndGet());
                return t;
            });
            workerGroup = new NioEventLoopGroup(r -> {
                Thread t = new Thread(r);
                t.setName("Netty IO Server Worker Thread #" + WORKER_THREAD_COUNT.incrementAndGet());
                return t;
            });
            clientWorkerGroup = new NioEventLoopGroup(r -> {
                Thread t = new Thread(r);
                t.setName("Netty IO Client Worker Thread #" + CLIENT_WORKER_THREAD_COUNT.incrementAndGet());
                return t;
            });
            LOGGER.info("Using normal channel type");
        }
    }

    public void listen(@NotNull ListenerInfo listenerInfo) {
        ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
            private boolean warningMessageShown = false;

            @Override
            protected void initChannel(@NotNull Channel ch) {
                try {
                    if (ch instanceof SocketChannel) {
                        ((SocketChannel) ch).config().setTcpNoDelay(true);
                    }
                } catch (ChannelException ignore) {
                }
                ch.pipeline()
                        .addFirst(new ReadTimeoutHandler(listenerInfo.getTimeout(), TimeUnit.MILLISECONDS))
                        .addFirst("rule_check_handler", RuleCheckHandler.INSTANCE);
                if (listenerInfo.isProxyProtocol()) {
                    ch.pipeline().addLast("haproxy_message_decoder", new HAProxyMessageDecoder());
                }
                if (!listenerInfo.getServers().isEmpty()) {
                    ServerInfo remoteServerInfo = listenerInfo.getServers().get(RANDOM.nextInt(listenerInfo.getServers().size()));
                    ch.pipeline().addLast("message_forwarder", new MessageForwarder(ch, listenerInfo, remoteServerInfo));
                } else if (!warningMessageShown) {
                    LOGGER.warn("Not adding message forwarder because listener for " + listenerInfo.getListenPort() + " has empty servers list");
                    warningMessageShown = true;
                }
                BuiltinTypeHandler.onConnectionInit(listenerInfo.getType(), ch);
                new ConnectionInitEvent(listenerInfo, ch).callEvent();
            }
        };
        ChannelFuture future;
        if (listenerInfo.getProtocol() == Protocol.TCP) {
            // TCP
            future = new ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(listenerInfo.getProtocol().serverChannelType.asSubclass(ServerChannel.class))
                    .childHandler(channelInitializer)
                    .bind(listenerInfo.getHost(), listenerInfo.getListenPort())
                    .syncUninterruptibly();
        } else {
            // UDP
            if (listenerInfo.getServers().isEmpty()) {
                LOGGER.warn("Not adding message forwarder because listener for " + listenerInfo.getListenPort() + " has empty servers list");
                future = new Bootstrap().group(workerGroup)
                        .channel(listenerInfo.getProtocol().serverChannelType)
                        .option(ChannelOption.SO_BROADCAST, true)
                        .option(ChannelOption.AUTO_READ, true)
                        .handler(new ChannelInitializer<DatagramChannel>() {
                            @Override
                            protected void initChannel(@NotNull DatagramChannel ch) {
                                BuiltinTypeHandler.onConnectionInit(listenerInfo.getType(), ch);
                                new ConnectionInitEvent(listenerInfo, ch).callEvent();
                            }
                        })
                        .bind(listenerInfo.getListenPort())
                        .syncUninterruptibly();
            } else {
                future = new Bootstrap().group(workerGroup)
                        .channel(listenerInfo.getProtocol().serverChannelType)
                        .option(ChannelOption.SO_BROADCAST, true)
                        .option(ChannelOption.AUTO_READ, true)
                        .handler(new UDPMessageForwarder(null, listenerInfo))
                        .bind(listenerInfo.getListenPort())
                        .syncUninterruptibly();
            }
        }
        if (listenerInfo.isProxyProtocol()) {
            LOGGER.warn("Proxy protocol enabled for listener {}, please ensure this listener is properly firewalled.", future.channel().toString());
        }
        LOGGER.info("Listening on {} ({})", future.channel().toString(), listenerInfo.getProtocol().name());
        futures.add(future);
    }

    @NotNull
    public ChannelFuture connect(@NotNull MessageForwarder forwarder, @NotNull ServerInfo serverInfo) {
        return new Bootstrap()
                .group(clientWorkerGroup)
                .channel(forwarder.listenerInfo.getProtocol().channelType)
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
                        new RemoteConnectionInitEvent(forwarder.listenerInfo, ch, forwarder.channel, forwarder.channel.remoteAddress()).callEvent();
                    }
                })
                .connect(serverInfo.getHost(), serverInfo.getPort());
    }

    @NotNull
    public ChannelFuture connect(@NotNull UDPMessageForwarder forwarder, @NotNull InetSocketAddress address, @NotNull ServerInfo serverInfo) {
        return new Bootstrap()
                .group(clientWorkerGroup)
                .channel(forwarder.listenerInfo.getProtocol().channelType)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(@NotNull Channel ch) {
                        ch.pipeline().addLast("message_forwarder_forwarder", new UDPMessageForwarderForwarder(forwarder, serverInfo, address));
                        if (serverInfo.isProxyProtocol()) {
                            ch.pipeline().addFirst("haproxy_message_encoder", HAProxyMessageEncoder.INSTANCE);
                        }
                        new RemoteConnectionInitEvent(forwarder.listenerInfo, ch, forwarder.channel, address).callEvent();
                    }
                })
                .connect(serverInfo.getHost(), serverInfo.getPort());
    }

    public void closeFutures() {
        for (ChannelFuture future : futures) {
            if (future.channel().isActive() || future.channel().isOpen()) {
                LOGGER.info("Closing future/listener: {}", future.channel().toString());
                future.channel().close().syncUninterruptibly();
            }
        }
        futures.clear();
    }

    public void close() {
        closeFutures();
        LOGGER.info("Shutting down event loop");
        clientWorkerGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
        bossGroup.shutdownGracefully().syncUninterruptibly();
    }
}
