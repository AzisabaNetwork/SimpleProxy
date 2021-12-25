package net.azisaba.simpleProxy.api.event;

import net.azisaba.simpleProxy.api.plugin.Plugin;
import net.azisaba.simpleProxy.api.util.ThrowableConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegisteredListener {
    @NotNull private final ThrowableConsumer<@NotNull Event> executor;
    @NotNull private final EventPriority priority;
    @Nullable private final Object listener;
    @NotNull private final Plugin plugin;

    public RegisteredListener(
            @NotNull ThrowableConsumer<@NotNull Event> executor,
            @NotNull EventPriority priority,
            @Nullable Object listener,
            @NotNull Plugin plugin) {
        this.executor = executor;
        this.priority = priority;
        this.listener = listener;
        this.plugin = plugin;
    }

    @NotNull
    public ThrowableConsumer<Event> getExecutor() {
        return executor;
    }

    @NotNull
    public EventPriority getPriority() {
        return priority;
    }

    @Nullable
    public Object getListener() {
        return listener;
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }
}
