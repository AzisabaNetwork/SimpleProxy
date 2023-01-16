package net.azisaba.simpleproxy.api.config;

public interface ProxyConfig {
    /**
     * Returns whether if the debug logging is enabled.
     * @return true if debug logging is enabled
     */
    boolean isDebug();

    /**
     * Returns whether if the verbose logging is enabled.
     * @return true if verbose logging is enabled
     */
    boolean isVerbose();

    /**
     * Checks if epoll is configured to true and available in current environment.
     * @return true if epoll is available
     */
    boolean isEpoll();
}
