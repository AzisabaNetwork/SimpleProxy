package net.azisaba.simpleproxy.api.event;

import net.azisaba.simpleproxy.api.ProxyServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event.
 * All events require a static method named getHandlerList() which returns the {@link HandlerList}.
 * @see Event#callEvent()
 */
@AbstractEvent
public abstract class Event {
    private String name;

    protected Event() {}

    /**
     * Calls the event and tests if cancelled.
     *
     * @return false if event was cancelled, if cancellable. true otherwise.
     */
    public boolean callEvent() {
        callEvent(this);
        if (this instanceof Cancellable) {
            return !((Cancellable) this).isCancelled();
        } else {
            return true;
        }
    }

    /**
     * Calls the event and returns this.
     * @param <T> the event type
     * @param event the event to call
     * @throws IllegalStateException when an event is fired from wrong thread.
     * @return the event
     */
    @Contract("_ -> param1")
    @NotNull
    public static <T extends Event> T callEvent(@NotNull T event) {
        return ProxyServer.getProxy().getEventManager().callEvent(event);
    }

    @NotNull
    public final String getEventName() {
        if (name == null) {
            name = getClass().getSimpleName();
        }
        return name;
    }

    @NotNull
    public final String getEventTypeName() {
        return getClass().getTypeName();
    }
}
