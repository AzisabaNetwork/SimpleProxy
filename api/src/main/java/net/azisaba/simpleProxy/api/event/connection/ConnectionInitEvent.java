package net.azisaba.simpleProxy.api.event.connection;

import io.netty.channel.Channel;
import net.azisaba.simpleProxy.api.event.Event;
import org.jetbrains.annotations.NotNull;

public class ConnectionInitEvent extends Event {
    private final Channel channel;

    public ConnectionInitEvent(@NotNull Channel channel) {
        this.channel = channel;
    }

    @NotNull
    public Channel getChannel() {
        return channel;
    }
}
