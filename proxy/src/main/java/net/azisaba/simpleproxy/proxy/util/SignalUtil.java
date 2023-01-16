package net.azisaba.simpleproxy.proxy.util;

import net.azisaba.simpleproxy.proxy.ProxyInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import sun.misc.Signal;

public class SignalUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean registeredShutdownHook = false;

    /**
     * Registers a signal handler for the given signal.
     * @param name The name of the signal to register a handler for. (e.g. "INT")
     * @param runnable the task to run when the signal is received.
     * @return true if the signal was successfully registered, false otherwise.
     */
    public static boolean register(@NotNull String name, @NotNull Runnable runnable) {
        try {
            Signal.handle(new Signal(name), sig -> runnable.run());
            return true;
        } catch (Throwable e) {
            LOGGER.debug("Error registering signal {}", name, e);
            return false;
        }
    }

    public static void registerAll() {
        if (register("TERM", () -> ProxyInstance.getInstance().stop())) {
            registeredShutdownHook = true;
        }
        if (register("INT", () -> ProxyInstance.getInstance().stop())) {
            registeredShutdownHook = true;
        }
        register("HUP", () -> ProxyInstance.getInstance().reloadConfig());

        if (!registeredShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> ProxyInstance.getInstance().stop()));
        }
    }
}
