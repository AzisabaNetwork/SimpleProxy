package net.azisaba.simpleproxy.api.event.connection;

import io.netty.channel.Channel;
import net.azisaba.simpleproxy.api.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class ChannelEvent extends Event {
    protected final Channel channel;

    public ChannelEvent(@NotNull Channel channel) {
        Objects.requireNonNull(channel, "channel");
        this.channel = channel;
    }

    @NotNull
    public Channel getChannel() {
        return channel;
    }
}
