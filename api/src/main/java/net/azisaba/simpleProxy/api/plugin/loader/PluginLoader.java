package net.azisaba.simpleProxy.api.plugin.loader;

import net.azisaba.simpleProxy.api.plugin.Plugin;
import net.azisaba.simpleProxy.api.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface PluginLoader {
    @NotNull
    Path getPluginsDir();

    void loadPlugins() throws IOException;

    @NotNull
    PluginDescriptionFile loadPluginDescriptionFile(@NotNull PluginFile pluginFile) throws IOException;

    void close() throws IOException;

    @NotNull
    List<Plugin> getPlugins();
}
