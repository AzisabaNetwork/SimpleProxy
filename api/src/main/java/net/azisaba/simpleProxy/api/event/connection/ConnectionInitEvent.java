package net.azisaba.simpleProxy.api.event.connection;

import io.netty.channel.Channel;
import net.azisaba.simpleProxy.api.config.ListenerInfo;
import org.jetbrains.annotations.NotNull;

public class ConnectionInitEvent extends ChannelEvent {
    private final ListenerInfo listenerInfo;

    public ConnectionInitEvent(@NotNull ListenerInfo listenerInfo, @NotNull Channel channel) {
        super(channel);
        this.listenerInfo = listenerInfo;
    }

    @NotNull
    public ListenerInfo getListenerInfo() {
        return listenerInfo;
    }
}
