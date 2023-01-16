package net.azisaba.simpleproxy.api.event.proxy;

import net.azisaba.simpleproxy.api.event.Event;

public class ProxyReloadEvent extends Event {
    public static final ProxyReloadEvent INSTANCE = new ProxyReloadEvent();

    private ProxyReloadEvent() {}
}
