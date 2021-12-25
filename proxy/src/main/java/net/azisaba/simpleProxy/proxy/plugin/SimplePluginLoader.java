package net.azisaba.simpleProxy.proxy.plugin;

import net.azisaba.simpleProxy.api.plugin.Plugin;
import net.azisaba.simpleProxy.api.plugin.PluginDescriptionFile;
import net.azisaba.simpleProxy.api.plugin.loader.PluginFile;
import net.azisaba.simpleProxy.api.plugin.loader.PluginLoader;
import net.azisaba.simpleProxy.api.yaml.YamlConfiguration;
import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimplePluginLoader implements PluginLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final PathMatcher JAR_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.jar");
    private final Path pluginsDir = Paths.get(".", "plugins").normalize();
    private final List<URLClassLoader> loaders = new ArrayList<>();
    private final List<Plugin> plugins = new ArrayList<>();

    public SimplePluginLoader() {
        if (!Files.exists(pluginsDir)) {
            try {
                Files.createDirectory(pluginsDir);
            } catch (IOException e) {
                LOGGER.error("Failed to create plugins directory", e);
            }
        } else if (Files.isRegularFile(pluginsDir)) {
            throw new RuntimeException("plugins is not a directory");
        }
    }

    @NotNull
    @Override
    public Path getPluginsDir() {
        return pluginsDir;
    }

    @Override
    public void loadPlugins() throws IOException {
        Files.walk(pluginsDir, FileVisitOption.FOLLOW_LINKS).forEach(path -> {
            if (!Files.isRegularFile(path)) return;
            if (!JAR_MATCHER.matches(path)) return;
            try {
                PluginFile pluginFile = new PluginFile(path.toFile());
                PluginDescriptionFile description = loadPluginDescriptionFile(pluginFile);
                URLClassLoader loader = new URLClassLoader(new URL[]{path.toUri().toURL()}, getClass().getClassLoader());
                Class<?> clazz;
                try {
                    clazz = loader.loadClass(description.main);
                } catch (ClassNotFoundException e) {
                    LOGGER.error("Failed to find main class of {}: {}", description.id, description.main, e);
                    loader.close();
                    return;
                }
                Class<? extends Plugin> pluginClass;
                try {
                    pluginClass = clazz.asSubclass(Plugin.class);
                } catch (ClassCastException e) {
                    LOGGER.error("Class {} does not extends {}", description.main, Plugin.class.getTypeName());
                    loader.close();
                    return;
                }
                Plugin plugin;
                try {
                    plugin = pluginClass.getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    LOGGER.error("Could not invoke constructor of {}", description.main);
                    loader.close();
                    return;
                }
                plugin.init(description);
                plugins.add(plugin);
                loaders.add(loader);
                LOGGER.info("Loaded plugin {} [{}] ({})", description.name, description.id, description.version);
            } catch (IOException e) {
                LOGGER.warn("Failed to load plugin {}", path, e);
            }
        });
    }

    @NotNull
    @Override
    public PluginDescriptionFile loadPluginDescriptionFile(@NotNull PluginFile pluginFile) throws IOException {
        InputStream in = pluginFile.getResourceAsStream("");
        if (in == null) throw new FileNotFoundException("plugin.yml does not exist");
        YamlObject obj = new YamlConfiguration(in).asObject();
        return PluginDescriptionFile.load(obj);
    }

    @Override
    public void close() throws IOException {
        for (URLClassLoader loader : loaders) loader.close();
        plugins.clear();
    }

    @NotNull
    @Override
    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }
}
