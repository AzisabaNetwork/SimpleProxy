package net.azisaba.simpleproxy.api.config;

import org.jetbrains.annotations.NotNull;

public interface ServerInfo {
    @NotNull
    String getHost();

    int getPort();

    boolean isProxyProtocol();
}
