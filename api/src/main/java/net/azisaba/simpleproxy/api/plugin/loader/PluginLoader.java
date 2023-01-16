package net.azisaba.simpleproxy.api.plugin.loader;

import net.azisaba.simpleproxy.api.plugin.Plugin;
import net.azisaba.simpleproxy.api.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface PluginLoader {
    /**
     * Returns the path of <code>plugins</code> folder.
     * @return the path
     */
    @NotNull
    Path getPluginsDir();

    /**
     * Loads all plugins in <code>plugins</code> folder.
     * @throws IOException if an input/output error occurs
     * @throws UnsupportedOperationException if plugin loader is not enabled
     */
    void loadPlugins() throws IOException;

    /**
     * Loads a single plugin.
     * @param description the description
     * @param path path to the plugin file
     * @throws IOException if an input/output error occurs
     * @throws UnsupportedOperationException if plugin loader is not enabled
     */
    void loadPlugin(@NotNull PluginDescriptionFile description, @NotNull Path path) throws IOException;

    /**
     * Returns loaded plugin by their plugin id.
     * @param id the id
     * @return plugin if loaded, null otherwise
     */
    @Nullable
    Plugin getPlugin(@NotNull String id);

    /**
     * Reads plugin.yml from plugin file and returns it. This method can be used even if plugin loader is not enabled.
     * @param pluginFile plugin file
     * @return plugin description file from plugin.yml
     * @throws IOException if plugin.yml does not exist
     */
    @NotNull
    PluginDescriptionFile loadPluginDescriptionFile(@NotNull PluginFile pluginFile) throws IOException;

    /**
     * Closes plugin loader.
     * @throws IOException if an input/output error occurs
     */
    void close() throws IOException;

    /**
     * Returns the list of all loaded plugins.
     * @return loaded plugins
     */
    @NotNull
    List<Plugin> getPlugins();

    /**
     * Checks if the plugin loader is enabled. If disabled, the plugin will not load.
     * @return whether if the plugin loader is enabled
     */
    boolean isEnabled();

    /**
     * Disables a plugin.
     * @param plugin the plugin to disable
     */
    void disablePlugin(@NotNull Plugin plugin);

    @Nullable
    Class<?> findClass(@NotNull String name);
}
