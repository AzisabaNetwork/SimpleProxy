package net.azisaba.simpleProxy.proxy;

import net.azisaba.simpleProxy.proxy.util.SignalUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        try {
            SignalUtil.registerAll();
            new ProxyInstance().start();
        } catch (Throwable throwable) {
            LOGGER.fatal("Failed to start proxy server", throwable);
        }
    }
}
