package net.azisaba.simpleproxy.api.event.proxy;

import net.azisaba.simpleproxy.api.event.Event;

public class ProxyInitializeEvent extends Event {
    public static final ProxyInitializeEvent INSTANCE = new ProxyInitializeEvent();

    private ProxyInitializeEvent() {}
}
