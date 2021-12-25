package net.azisaba.simpleProxy.api.event.connection;

import io.netty.channel.Channel;
import net.azisaba.simpleProxy.api.event.Event;
import org.jetbrains.annotations.NotNull;

public class RemoteConnectionInitEvent extends Event {
    private final Channel channel;

    public RemoteConnectionInitEvent(@NotNull Channel channel) {
        this.channel = channel;
    }

    @NotNull
    public Channel getChannel() {
        return channel;
    }
}
