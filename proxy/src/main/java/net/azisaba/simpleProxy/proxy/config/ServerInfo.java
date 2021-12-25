package net.azisaba.simpleProxy.proxy.config;

import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ServerInfo {
    private final String host;
    private final int port;
    private final boolean proxyProtocol;

    public ServerInfo(@NotNull String host, int port, boolean proxyProtocol) {
        Objects.requireNonNull(host, "host cannot be null");
        if (port <= 0 || port > 65535) throw new RuntimeException("Port is out of range: " + port);
        this.host = host;
        this.port = port;
        this.proxyProtocol = proxyProtocol;
    }

    public ServerInfo(@NotNull YamlObject obj) {
        this(obj.getString("host"), obj.getInt("port"), obj.getBoolean("proxyProtocol", false));
    }

    @NotNull
    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public boolean isProxyProtocol() {
        return proxyProtocol;
    }
}
