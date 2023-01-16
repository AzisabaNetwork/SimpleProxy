package net.azisaba.simpleproxy.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ProxyServerHolder {
    private static ProxyServer proxyServer;

    public static void setProxyServer(@NotNull ProxyServer proxyServer) {
        if (ProxyServerHolder.proxyServer != null) {
            throw new IllegalStateException("Cannot redefine ProxyServer singleton");
        }
        ProxyServerHolder.proxyServer = proxyServer;
    }

    @Contract(pure = true)
    @NotNull
    public static Optional<ProxyServer> getProxyServer() {
        return Optional.ofNullable(proxyServer);
    }
}
