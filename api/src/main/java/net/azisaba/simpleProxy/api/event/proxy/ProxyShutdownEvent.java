package net.azisaba.simpleProxy.api.event.proxy;

import net.azisaba.simpleProxy.api.event.Event;

public class ProxyShutdownEvent extends Event {
    public static final ProxyShutdownEvent INSTANCE = new ProxyShutdownEvent();

    private ProxyShutdownEvent() {}
}
