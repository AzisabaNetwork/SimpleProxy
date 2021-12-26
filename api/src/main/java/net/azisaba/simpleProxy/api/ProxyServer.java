package net.azisaba.simpleProxy.api;

import net.azisaba.simpleProxy.api.command.CommandManager;
import net.azisaba.simpleProxy.api.event.EventManager;
import net.azisaba.simpleProxy.api.plugin.loader.PluginLoader;
import net.azisaba.simpleProxy.api.util.ProxyVersion;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface ProxyServer {
    /**
     * Returns the proxy server instance.
     * @return the proxy server
     */
    @NotNull
    static ProxyServer getProxy() {
        return ProxyServerHolder.getProxyServer().orElseThrow(() -> new IllegalStateException("Proxy server isn't initialized (yet)"));
    }

    /**
     * Returns a command manager for this proxy server.
     * @return the command manager
     */
    @NotNull
    CommandManager getCommandManager();

    /**
     * Reloads the proxy config and returns the completable future.
     * @return completable future of reload task
     */
    @NotNull
    CompletableFuture<Void> reloadConfig();

    /**
     * Returns the proxy software version information.
     * @return version information
     */
    @NotNull
    ProxyVersion getVersion();

    /**
     * Returns event manager for this proxy server.
     * @return the event manager
     */
    @NotNull
    EventManager getEventManager();

    /**
     * Returns plugin loader for this proxy server.
     * @return the plugin loader
     */
    @NotNull
    PluginLoader getPluginLoader();
}
