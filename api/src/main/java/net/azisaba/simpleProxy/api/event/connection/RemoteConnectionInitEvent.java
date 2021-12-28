package net.azisaba.simpleProxy.api.event.connection;

import io.netty.channel.Channel;
import net.azisaba.simpleProxy.api.config.ListenerInfo;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public class RemoteConnectionInitEvent extends ConnectionInitEvent {
    private final Channel sourceChannel;
    private final SocketAddress sourceAddress;

    public RemoteConnectionInitEvent(@NotNull ListenerInfo listenerInfo, @NotNull Channel channel, @NotNull Channel sourceChannel, @NotNull SocketAddress sourceAddress) {
        super(listenerInfo, channel);
        this.sourceChannel = sourceChannel;
        this.sourceAddress = sourceAddress;
    }

    /**
     * Returns the source channel (= user's channel, not remote's channel) of connection.
     * @return source channel
     */
    @NotNull
    public Channel getSourceChannel() {
        return sourceChannel;
    }

    /**
     * Returns the source address (= user's ip, not proxy/remote's ip) of connection. Returned type of value is
     * always InetSocketAddress for {@link net.azisaba.simpleProxy.api.config.Protocol#UDP UDP}.
     * @return source address
     */
    @NotNull
    public SocketAddress getSourceAddress() {
        return sourceAddress;
    }
}
