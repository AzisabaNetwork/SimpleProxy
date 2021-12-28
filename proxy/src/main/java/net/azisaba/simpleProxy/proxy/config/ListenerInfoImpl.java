package net.azisaba.simpleProxy.proxy.config;

import net.azisaba.simpleProxy.api.config.ListenerInfo;
import net.azisaba.simpleProxy.api.config.Protocol;
import net.azisaba.simpleProxy.api.config.ServerInfo;
import net.azisaba.simpleProxy.api.yaml.YamlConfiguration;
import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ListenerInfoImpl implements ListenerInfo {
    private final String host;
    private final int listenPort;
    private final List<ServerInfo> servers;
    private final boolean proxyProtocol;
    private final int timeout;
    private final Protocol protocol;
    private final String type;

    public ListenerInfoImpl(@NotNull String host,
                            int listenPort,
                            @NotNull List<ServerInfo> servers,
                            boolean proxyProtocol,
                            int timeout,
                            @NotNull Protocol protocol,
                            @Nullable String type) {
        if (listenPort <= 0 || listenPort > 65535) throw new RuntimeException("Port is out of range: " + listenPort);
        Objects.requireNonNull(host, "host cannot be null");
        Objects.requireNonNull(servers, "servers cannot be null");
        this.host = host;
        this.listenPort = listenPort;
        this.servers = servers;
        this.proxyProtocol = proxyProtocol;
        this.timeout = timeout;
        this.protocol = protocol;
        this.type = type;
    }

    public ListenerInfoImpl(@NotNull YamlObject obj) {
        this(
                obj.getString("host", "0.0.0.0"),
                obj.getInt("listenPort"),
                Objects.requireNonNull(obj.getArray("servers")).<Map<String, Object>, ServerInfo>mapAsType(map ->
                        new ServerInfoImpl(new YamlObject(YamlConfiguration.DEFAULT, map))
                ),
                obj.getBoolean("proxyProtocol", false),
                obj.getInt("timeout", 1000 * 30), // 30 seconds
                Protocol.valueOf(obj.getString("protocol", "tcp").toUpperCase(Locale.ROOT)),
                obj.getString("type")
        );
    }

    @NotNull
    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getListenPort() {
        return listenPort;
    }

    @NotNull
    @Override
    public List<ServerInfo> getServers() {
        return servers;
    }

    @Override
    public boolean isProxyProtocol() {
        return proxyProtocol;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @NotNull
    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    @Nullable
    @Override
    public String getType() {
        return type;
    }
}