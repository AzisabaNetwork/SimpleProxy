package net.azisaba.simpleproxy.api.event;

/**
 * CancellableEvent (API)
 * <p>This event can be overridden, so you don't have to implement isCancelled/setCancelled method.
 */
public abstract class CancellableEvent extends Event implements Cancellable {
    protected boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
