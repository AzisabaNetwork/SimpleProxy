package net.azisaba.simpleProxy.api.event.proxy;

import net.azisaba.simpleProxy.api.event.Event;

public class ProxyInitializeEvent extends Event {
    public static final ProxyInitializeEvent INSTANCE = new ProxyInitializeEvent();

    private ProxyInitializeEvent() {}
}
