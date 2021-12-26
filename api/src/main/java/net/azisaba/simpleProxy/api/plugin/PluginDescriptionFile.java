package net.azisaba.simpleProxy.api.plugin;

import net.azisaba.simpleProxy.api.yaml.YamlArray;
import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PluginDescriptionFile {
    @NotNull public final String id;
    @NotNull public final String name;
    @NotNull public final String version;
    @NotNull public final String main;
    @NotNull public final List<String> depends;
    @NotNull public final List<String> softDepend;

    private PluginDescriptionFile(@NotNull String id,
                                 @NotNull String name,
                                 @NotNull String version,
                                 @NotNull String main,
                                 @NotNull List<String> depends,
                                 @NotNull List<String> softDepend) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.main = main;
        this.depends = depends;
        this.softDepend = softDepend;
    }

    @Contract("_ -> new")
    @NotNull
    public static PluginDescriptionFile load(@NotNull YamlObject obj) {
        String id = obj.getString("id");
        String name = obj.getString("name");
        if (id == null) id = name;
        if (name == null) name = id;
        String version = obj.getString("version");
        String main = obj.getString("main");
        Objects.requireNonNull(id, "Required element 'id' or 'name' is missing");
        Objects.requireNonNull(version, "Required element 'version' is missing");
        Objects.requireNonNull(main, "Required element 'main' is missing");
        List<String> depends = Optional.ofNullable(obj.getArray("depends")).map(YamlArray::mapToString).orElse(Collections.emptyList());
        List<String> softDepend = Optional.ofNullable(obj.getArray("softdepend")).map(YamlArray::mapToString).orElse(Collections.emptyList());
        return new PluginDescriptionFile(id, name, version, main, depends, softDepend);
    }
}
