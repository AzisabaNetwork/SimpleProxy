package net.azisaba.simpleProxy.api.config;

import org.jetbrains.annotations.NotNull;

public interface ServerInfo {
    @NotNull
    String getHost();

    int getPort();

    boolean isProxyProtocol();
}
