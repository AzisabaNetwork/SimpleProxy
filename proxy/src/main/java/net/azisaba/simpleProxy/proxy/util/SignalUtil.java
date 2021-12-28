package net.azisaba.simpleProxy.proxy.util;

import net.azisaba.simpleProxy.proxy.ProxyInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import sun.misc.Signal;

public class SignalUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(@NotNull String name, @NotNull Runnable runnable) {
        try {
            Signal.handle(new Signal(name), sig -> runnable.run());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Error registering signal {}", name, e);
        }
    }

    public static void registerAll() {
        register("TERM", () -> ProxyInstance.getInstance().stop());
        register("INT", () -> ProxyInstance.getInstance().stop());
        register("HUP", () -> ProxyInstance.getInstance().reloadConfig());
    }
}
