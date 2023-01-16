package net.azisaba.simpleproxy.api.event;

public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean cancel);
}
