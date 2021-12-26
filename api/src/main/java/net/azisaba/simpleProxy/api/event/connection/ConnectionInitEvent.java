package net.azisaba.simpleProxy.api.event.connection;

import io.netty.channel.Channel;
import net.azisaba.simpleProxy.api.config.ListenerInfo;
import net.azisaba.simpleProxy.api.event.Event;
import org.jetbrains.annotations.NotNull;

public class ConnectionInitEvent extends Event {
    private final ListenerInfo listenerInfo;
    private final Channel channel;

    public ConnectionInitEvent(@NotNull ListenerInfo listenerInfo, @NotNull Channel channel) {
        this.listenerInfo = listenerInfo;
        this.channel = channel;
    }

    @NotNull
    public ListenerInfo getListenerInfo() {
        return listenerInfo;
    }

    @NotNull
    public Channel getChannel() {
        return channel;
    }
}
