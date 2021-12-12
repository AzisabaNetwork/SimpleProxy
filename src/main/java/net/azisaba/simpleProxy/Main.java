package net.azisaba.simpleProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        try {
            new ProxyServer().start();
        } catch (Throwable throwable) {
            LOGGER.fatal("Failed to start proxy server", throwable);
        }
    }
}
