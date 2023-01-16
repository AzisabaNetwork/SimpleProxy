package net.azisaba.simpleproxy.api.event.proxy;

import net.azisaba.simpleproxy.api.event.Event;

public class ProxyShutdownEvent extends Event {
    public static final ProxyShutdownEvent INSTANCE = new ProxyShutdownEvent();

    private ProxyShutdownEvent() {}
}
