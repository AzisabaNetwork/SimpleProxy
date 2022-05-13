package net.azisaba.simpleProxy.api;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleProxy.api.command.CommandManager;
import net.azisaba.simpleProxy.api.config.ListenerInfo;
import net.azisaba.simpleProxy.api.config.ProxyConfig;
import net.azisaba.simpleProxy.api.event.EventManager;
import net.azisaba.simpleProxy.api.plugin.loader.PluginLoader;
import net.azisaba.simpleProxy.api.util.ProxyVersion;
import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.jetbrains.annotations.Contract;
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

    /**
     * Returns the proxy config.
     * @return proxy config
     */
    @NotNull
    ProxyConfig getConfig();

    /**
     * Closes all active connections and shutdown the proxy server.
     */
    void stop();

    /**
     * Returns the instance which contains the methods which may be changed at any time.
     * @return the unsafe instance
     */
    @NotNull
    Unsafe unsafe();

    interface Unsafe {
        @Contract(value = "_, _ -> new", pure = true)
        @NotNull
        ChannelInboundHandlerAdapter createMessageForwarder(@NotNull Channel ch, @NotNull ListenerInfo listenerInfo);

        @Contract(value = "_ -> new", pure = true)
        @NotNull
        ListenerInfo createListenerInfo(@NotNull YamlObject obj);
    }
}
