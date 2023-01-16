package net.azisaba.simpleproxy.proxy;

import net.azisaba.simpleproxy.proxy.util.MemoryReserve;
import net.azisaba.simpleproxy.proxy.util.SignalUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        try {
            preload();
            SignalUtil.registerAll();
            MemoryReserve.reserve();
            new ProxyInstance().start();
        } catch (Throwable throwable) {
            LOGGER.fatal("Failed to start proxy server", throwable);
        }
    }

    private static void preload() {
        try {
            // avoid stuck on shutdown if jar is replaced before shutdown
            Class.forName("net.azisaba.simpleproxy.api.event.proxy.ProxyShutdownEvent");
            Class.forName("org.apache.logging.log4j.core.async.InternalAsyncUtil");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
