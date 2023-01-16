package net.azisaba.simpleproxy.api.event.connection;

import io.netty.channel.Channel;
import net.azisaba.simpleproxy.api.config.ListenerInfo;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

/**
 * Fired when the remote connection is active and now ready to send/receive data.
 */
public class RemoteConnectionActiveEvent extends RemoteConnectionInitEvent {
    public RemoteConnectionActiveEvent(@NotNull ListenerInfo listenerInfo, @NotNull Channel channel, @NotNull Channel sourceChannel, @NotNull SocketAddress sourceAddress) {
        super(listenerInfo, channel, sourceChannel, sourceAddress);
    }
}
