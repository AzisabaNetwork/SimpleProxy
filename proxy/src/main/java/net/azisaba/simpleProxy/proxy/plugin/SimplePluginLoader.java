package net.azisaba.simpleProxy.proxy.plugin;

import net.azisaba.simpleProxy.api.plugin.Plugin;
import net.azisaba.simpleProxy.api.plugin.PluginDescriptionFile;
import net.azisaba.simpleProxy.api.plugin.loader.PluginFile;
import net.azisaba.simpleProxy.api.plugin.loader.PluginLoader;
import net.azisaba.simpleProxy.api.yaml.YamlConfiguration;
import net.azisaba.simpleProxy.api.yaml.YamlObject;
import net.azisaba.simpleProxy.proxy.ProxyInstance;
import net.azisaba.simpleProxy.proxy.config.ProxyConfigInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class SimplePluginLoader implements PluginLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Path pluginsDir = Paths.get(".", "plugins").normalize();
    private final List<PluginClassLoader> loaders = new ArrayList<>();
    private final List<Plugin> plugins = new ArrayList<>();
    private final Map<String, PluginPreloadData> preloadData = new ConcurrentHashMap<>();
    private final Map<String, Plugin> id2PluginMap = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

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
        if (!isEnabled()) throw new UnsupportedOperationException("Plugin loader is not enabled");
        try (Stream<Path> stream = Files.walk(pluginsDir, FileVisitOption.FOLLOW_LINKS)) {
            stream.forEach(path -> {
                if (!Files.isRegularFile(path)) return;
                if (!path.getFileName().toString().endsWith(".jar")) return;
                try {
                    PluginDescriptionFile description;
                    try (PluginFile pluginFile = new PluginFile(path.toFile())) {
                        description = loadPluginDescriptionFile(pluginFile);
                    }
                    preloadData.put(description.id, new PluginPreloadData(description, path));
                } catch (IOException e) {
                    LOGGER.error("Failed to preload plugin {}", path, e);
                }
            });
        }
        for (PluginPreloadData data : new ArrayList<>(preloadData.values())) {
            try {
                loadPlugin(data.description, data.path);
            } catch (Throwable e) {
                LOGGER.error("Failed to load plugin {}", data.description.id, e);
                if (e instanceof VirtualMachineError) throw e;
            }
        }
    }

    @Override
    public void loadPlugin(@NotNull PluginDescriptionFile description, @NotNull Path path) throws IOException {
        if (!isEnabled()) throw new UnsupportedOperationException("Plugin loader is not enabled");
        if (id2PluginMap.containsKey(description.id)) return; // already loaded
        List<String> missingDependencies = new ArrayList<>();
        for (String depend : description.depends) {
            if (description.id.equals(depend)) throw new RuntimeException("Depends on itself");
            PluginPreloadData data = preloadData.get(depend);
            if (data == null) missingDependencies.add(depend);
        }
        if (!missingDependencies.isEmpty()) {
            String joined = String.join(", ", missingDependencies);
            throw new RuntimeException("Missing required dependencies: " + joined);
        }
        for (String depend : description.depends) {
            PluginPreloadData data = preloadData.get(depend);
            try {
                loadPlugin(data.description, data.path);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load plugin " + data.description.id, e);
            }
        }
        for (String depend : description.softDepend) {
            if (description.id.equals(depend)) throw new RuntimeException("Depends on itself");
            PluginPreloadData data = preloadData.get(depend);
            if (data == null) {
                LOGGER.debug("Optional dependency {} of {} is missing", depend, description.id);
                continue;
            }
            try {
                loadPlugin(data.description, data.path);
            } catch (Exception e) {
                LOGGER.error("Failed to load optional dependency {} of {}", data.description.id, description.id, e);
            }
        }
        PluginClassLoader loader = new PluginClassLoader(this, new URL[]{path.toUri().toURL()}, getClass().getClassLoader());
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
        id2PluginMap.put(description.id, plugin);
        LOGGER.info("Loaded plugin {} [{}] ({})", description.name, description.id, description.version);
    }

    @Nullable
    @Override
    public Plugin getPlugin(@NotNull String id) {
        Objects.requireNonNull(id, "id cannot be null");
        for (Plugin plugin : plugins) {
            if (id.equals(plugin.getId())) return plugin;
        }
        return null;
    }

    @NotNull
    @Override
    public PluginDescriptionFile loadPluginDescriptionFile(@NotNull PluginFile pluginFile) throws IOException {
        InputStream in = pluginFile.getResourceAsStream("plugin.yml");
        if (in == null) throw new FileNotFoundException("plugin.yml does not exist");
        YamlObject obj = new YamlConfiguration(in).asObject();
        return PluginDescriptionFile.load(obj);
    }

    @Override
    public void close() throws IOException {
        for (Plugin plugin : plugins) {
            disablePlugin(plugin);
            id2PluginMap.remove(plugin.getDescription().id);
        }
        for (URLClassLoader loader : loaders) loader.close();
        plugins.clear();
    }

    @NotNull
    @Override
    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    @Override
    public boolean isEnabled() {
        return !ProxyConfigInstance.disablePlugins;
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        ProxyInstance.getInstance().getEventManager().unregisterEvents(plugin);
        LOGGER.info("Disabled plugin {} [{}] ({})", plugin.getName(), plugin.getId(), plugin.getDescription().version);
    }

    @Nullable
    @Override
    public Class<?> findClass(@NotNull String name) {
        Class<?> result = classes.get(name);
        if (result != null) return result;
        try {
            result = ClassLoader.getSystemClassLoader().loadClass(name);
        } catch (ClassNotFoundException ignore) {}
        if (result != null) {
            setClass(name, result);
            return result;
        }
        for (PluginClassLoader loader : loaders) {
            try {
                result = loader.findClass(name, false);
            } catch (ClassNotFoundException ignore) {}
            if (result != null) {
                setClass(name, result);
                return result;
            }
        }
        return null;
    }

    protected void setClass(@NotNull String name, @NotNull Class<?> clazz) {
        if (!classes.containsKey(name)) {
            classes.put(name, clazz);
        }
    }

    private static class PluginPreloadData {
        private final PluginDescriptionFile description;
        private final Path path;

        public PluginPreloadData(PluginDescriptionFile description, Path path) {
            this.description = description;
            this.path = path;
        }
    }
}
