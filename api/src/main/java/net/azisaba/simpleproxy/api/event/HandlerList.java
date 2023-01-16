package net.azisaba.simpleproxy.api.event;

import net.azisaba.simpleproxy.api.plugin.Plugin;
import net.azisaba.simpleproxy.api.util.ThrowableConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HandlerList {
    private static final Logger LOGGER = LogManager.getLogger();
    @NotNull
    private final Set<RegisteredListener> listeners = Collections.synchronizedSet(new HashSet<>());

    public void add(@NotNull ThrowableConsumer<@NotNull Event> consumer, @NotNull EventPriority priority, @Nullable Object listener, @NotNull Plugin plugin) {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(priority);
        Objects.requireNonNull(plugin);
        listeners.add(new RegisteredListener(consumer, priority, listener, plugin));
    }

    public void remove(@NotNull Plugin plugin) {
        Objects.requireNonNull(plugin);
        List<RegisteredListener> toRemove = new ArrayList<>();
        listeners.forEach(registeredListener -> {
            if (plugin.equals(registeredListener.getPlugin())) {
                toRemove.add(registeredListener);
            }
        });
        toRemove.forEach(listeners::remove);
    }

    public boolean anyContains(@NotNull Plugin plugin) {
        Objects.requireNonNull(plugin);
        for (RegisteredListener listener : listeners) {
            if (plugin.equals(listener.getPlugin())) return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public int size() {
        return listeners.size();
    }

    public void remove(@NotNull Object listener) {
        Objects.requireNonNull(listener);
        List<RegisteredListener> toRemove = new ArrayList<>();
        listeners.forEach(registeredListener -> {
            if (listener.equals(registeredListener.getListener())) {
                toRemove.add(registeredListener);
            }
        });
        toRemove.forEach(listeners::remove);
    }

    public void fire(@NotNull Event event) {
        Objects.requireNonNull(event);
        this.listeners
                .stream()
                .sorted(Comparator.comparingInt(registeredListener -> registeredListener.getPriority().getSlot()))
                .forEach(registeredListener -> {
                    try {
                        registeredListener.getExecutor().accept(event);
                    } catch (Throwable e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        String listenerName = registeredListener.getListener() == null ? null : registeredListener.getListener().getClass().getTypeName();
                        LOGGER.error("", new EventException("Could not pass event " + event.getEventTypeName() + " to listener " + listenerName + " of plugin " + registeredListener.getPlugin().getName(), cause));
                    }
                });
    }
}
