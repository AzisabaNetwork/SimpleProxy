package net.azisaba.simpleProxy.api.config;

import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    String getType();

    /**
     * Returns the raw config object.
     * @return the config
     */
    @NotNull
    YamlObject getConfig();
}
