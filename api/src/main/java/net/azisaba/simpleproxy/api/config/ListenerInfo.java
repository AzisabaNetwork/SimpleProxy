package net.azisaba.simpleproxy.api.config;

import net.azisaba.simpleproxy.api.yaml.YamlObject;
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

    int getInitialTimeout();

    int getTimeout();

    @NotNull
    Protocol getProtocol();

    @Nullable
    String getType();

    boolean isConnectOnActive();

    /**
     * Returns the raw config object.
     * @return the config
     */
    @NotNull
    YamlObject getConfig();
}
