package net.azisaba.simpleProxy.api.plugin;

import net.azisaba.simpleProxy.api.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a plugin. All main class of plugin <strong>must</strong> <code>extends</code> this class.
 * Event listeners are automatically registered for main class.
 */
public class Plugin {
    private PluginDescriptionFile description;

    /**
     * You really should not call this method.
     */
    public final void init(@NotNull PluginDescriptionFile description) {
        if (this.description != null) throw new RuntimeException("You called init after or inside constructor");
        this.description = description;
        getProxy().getEventManager().registerEvents(this, this);
    }

    @NotNull
    public ProxyServer getProxy() {
        return ProxyServer.getProxy();
    }

    @NotNull
    public PluginDescriptionFile getDescription() {
        return Objects.requireNonNull(description);
    }

    @NotNull
    public String getName() {
        return description.name;
    }

    @NotNull
    public String getId() {
        return description.id;
    }
}
