package net.azisaba.simpleProxy.config;

import net.azisaba.simpleProxy.yaml.YamlConfiguration;
import net.azisaba.simpleProxy.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListenerInfo {
    private final int listenPort;
    private final List<ServerInfo> servers;
    private final boolean proxyProtocol;
    private final int timeout;

    public ListenerInfo(int listenPort, @NotNull List<ServerInfo> servers, boolean proxyProtocol, int timeout) {
        if (listenPort <= 0 || listenPort > 65535) throw new RuntimeException("Port is out of range: " + listenPort);
        this.listenPort = listenPort;
        this.servers = servers;
        this.proxyProtocol = proxyProtocol;
        this.timeout = timeout;
    }

    public ListenerInfo(@NotNull YamlObject obj) {
        this(
                obj.getInt("listenPort"),
                Objects.requireNonNull(obj.getArray("servers")).<Map<String, Object>, ServerInfo>mapAsType(map ->
                        new ServerInfo(new YamlObject(YamlConfiguration.DEFAULT, map))
                ),
                obj.getBoolean("proxyProtocol", false),
                obj.getInt("timeout", 1000 * 30) // 30 seconds
        );
    }

    public int getListenPort() {
        return listenPort;
    }

    @NotNull
    public List<ServerInfo> getServers() {
        return servers;
    }

    public boolean isProxyProtocol() {
        return proxyProtocol;
    }

    public int getTimeout() {
        return timeout;
    }
}
