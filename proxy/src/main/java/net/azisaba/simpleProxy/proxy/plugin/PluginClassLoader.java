package net.azisaba.simpleProxy.proxy.plugin;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginClassLoader extends URLClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    protected final SimplePluginLoader pluginLoader;
    protected final List<String> knownNotExist = Collections.synchronizedList(new ArrayList<>());
    protected final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    public PluginClassLoader(SimplePluginLoader pluginLoader, @NotNull URL@NotNull[] urls, @NotNull ClassLoader parent) {
        super(urls, parent);
        this.pluginLoader = pluginLoader;
    }

    @NotNull
    @Override
    public Class<?> loadClass(@NotNull String name) throws ClassNotFoundException {
        Class<?> clazz = this.findLoadedClass(name);
        if (clazz != null) return clazz;
        try {
            return this.findClass(name, true);
        } catch (ClassNotFoundException ignore) {}
        return super.loadClass(name);
    }

    @NotNull
    @Override
    protected Class<?> findClass(@NotNull String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    @NotNull
    public Class<?> findClass(@NotNull String name, boolean checkGlobal) throws ClassNotFoundException {
        if (knownNotExist.contains(name)) throw new ClassNotFoundException(name);
        Class<?> result = classes.get(name);
        if (result != null) return result;
        if (checkGlobal) {
            result = pluginLoader.findClass(name);
        }
        if (result == null) {
            try {
                result = super.findClass(name);
            } catch (ClassNotFoundException ignore) {}
        }
        if (result == null) {
            knownNotExist.add(name);
            throw new ClassNotFoundException(name);
        }
        classes.put(name, result);
        pluginLoader.setClass(name, result);
        return result;
    }
}
