package net.azisaba.simpleproxy.proxy.config;

import net.azisaba.simpleproxy.api.config.ServerInfo;
import net.azisaba.simpleproxy.api.yaml.YamlObject;
import org.jetbrains.annotations.Contract;
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

    @Override
    public String toString() {
        return "ServerInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", proxyProtocol=" + proxyProtocol +
                '}';
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerInfoImpl)) return false;
        ServerInfoImpl that = (ServerInfoImpl) o;
        return getPort() == that.getPort() && isProxyProtocol() == that.isProxyProtocol() && getHost().equals(that.getHost());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getPort(), isProxyProtocol());
    }
}
