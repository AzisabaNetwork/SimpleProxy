package net.azisaba.simpleProxy.api.util;

import org.jetbrains.annotations.NotNull;

public interface ProxyVersion {
    /**
     * Returns the proxy software name.
     * @return proxy software name
     */
    @NotNull
    String getName();

    /**
     * Returns the proxy software version.
     * @return proxy software version
     */
    @NotNull
    String getVersion();
}
