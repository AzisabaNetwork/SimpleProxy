package net.azisaba.simpleProxy.api.event;

import net.azisaba.simpleProxy.api.plugin.Plugin;
import net.azisaba.simpleProxy.api.util.ThrowableConsumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Set;

public interface EventManager {
    /**
     * Register a listener. To qualify a method for listener, the method would need to meet all these requirements:
     * <ul>
     *     <li>Parameter count is 1</li>
     *     <li>Parameter #1 is a event (like PlayerBlockBreakEvent)</li>
     *     <li>Event class must have a {@link Event valid getHandlerList() method} and public modifier</li>
     *     <li>Method is accessible from EventManager (usually method should be <code>public</code>)</li>
     *     <li>Must not be abstract</li>
     *     <li>Return type should be void (return value isn't used for now)</li>
     *     <li>Method can be static or instance method</li>
     * </ul>
     * @param plugin the plugin
     * @param listener the listener
     */
    void registerEvents(@NotNull Plugin plugin, @NotNull Object listener);

    /**
     * Register a listener.
     * @param clazz the event class
     * @param plugin the plugin
     * @param priority event priority
     * @param consumer event handler
     * @param <T> the event type
     */
    <T extends Event> void registerEvent(@NotNull Class<T> clazz, @NotNull Plugin plugin, @NotNull EventPriority priority, @NotNull ThrowableConsumer<T> consumer);

    /**
     * Unregister all listeners associated with a provided plugin.
     * @param plugin the plugin
     */
    void unregisterEvents(@NotNull Plugin plugin);

    /**
     * Unregister a listener
     * @param listener the listener to unregister
     */
    void unregisterEvents(@NotNull Object listener);

    /**
     * Calls an event.
     * @param event the event
     * @throws IllegalStateException when an event is fired from wrong thread.
     * @return the fired event
     */
    @Contract("_ -> param1")
    @NotNull
    <T extends Event> T callEvent(@NotNull T event);

    /**
     * Returns the set of all events known to EventManager.
     * @return all known events
     */
    @NotNull
    Set<Class<? extends Event>> getKnownEvents();

    @Contract(pure = true)
    @NotNull
    @Unmodifiable
    Map<Class<? extends Event>, HandlerList> getHandlerMap();

    /**
     * Returns a handler list for an event class.
     * @param event the class of an event
     * @return handler list
     * @throws IllegalArgumentException if event class is annotated with @AbstractEvent or is abstract class
     */
    @NotNull
    HandlerList getHandlerList(@NotNull Class<? extends Event> event);
}
