package net.azisaba.simpleProxy.api.config;

import io.netty.channel.Channel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

@SuppressWarnings("unused")
public enum Protocol {
    TCP(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class, Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class),
    UDP(Epoll.isAvailable() ? EpollDatagramChannel.class : NioDatagramChannel.class, Epoll.isAvailable() ? EpollDatagramChannel.class : NioDatagramChannel.class),
    ;

    public final Class<? extends Channel> serverChannelType;
    public final Class<? extends Channel> channelType;

    Protocol(Class<? extends Channel> serverChannelType, Class<? extends Channel> channelType) {
        this.serverChannelType = serverChannelType;
        this.channelType = channelType;
    }
}
