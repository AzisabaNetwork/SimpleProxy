package net.azisaba.simpleProxy.api.config;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ListenerInfo {
    @NotNull
    String getHost();

    int getListenPort();

    @NotNull
    List<ServerInfo> getServers();

    boolean isProxyProtocol();

    int getTimeout();

    @NotNull
    Protocol getProtocol();

    @NotNull
    String getType();
}
