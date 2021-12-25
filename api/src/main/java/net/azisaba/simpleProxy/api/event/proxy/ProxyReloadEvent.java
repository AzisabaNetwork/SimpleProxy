package net.azisaba.simpleProxy.api.event.proxy;

import net.azisaba.simpleProxy.api.event.Event;

public class ProxyReloadEvent extends Event {
    public static final ProxyReloadEvent INSTANCE = new ProxyReloadEvent();

    private ProxyReloadEvent() {}
}
