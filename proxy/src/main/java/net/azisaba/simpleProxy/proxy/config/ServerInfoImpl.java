package net.azisaba.simpleProxy.proxy.config;

import net.azisaba.simpleProxy.api.config.ServerInfo;
import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ServerInfoImpl implements ServerInfo {
    private final String host;
    private final int port;
    private final boolean proxyProtocol;

    public ServerInfoImpl(@NotNull String host, int port, boolean proxyProtocol) {
        Objects.requireNonNull(host, "host cannot be null");
        if (port <= 0 || port > 65535) throw new RuntimeException("Port is out of range: " + port);
        this.host = host;
        this.port = port;
        this.proxyProtocol = proxyProtocol;
    }

    public ServerInfoImpl(@NotNull YamlObject obj) {
        this(obj.getString("host"), obj.getInt("port"), obj.getBoolean("proxyProtocol", false));
    }

    @NotNull
    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public boolean isProxyProtocol() {
        return proxyProtocol;
    }
}
