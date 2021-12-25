package net.azisaba.simpleProxy.api.plugin;

import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PluginDescriptionFile {
    @NotNull public final String id;
    @NotNull public final String name;
    @NotNull public final String version;
    @NotNull public final String main;

    public PluginDescriptionFile(@NotNull String id,
                                 @NotNull String name,
                                 @NotNull String version,
                                 @NotNull String main) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.main = main;
    }

    @Contract("_ -> new")
    @NotNull
    public static PluginDescriptionFile load(@NotNull YamlObject obj) {
        String id = obj.getString("id");
        String version = obj.getString("version");
        String main = obj.getString("main");
        Objects.requireNonNull(id, "Required element 'main' is missing");
        Objects.requireNonNull(version, "Required element 'main' is missing");
        Objects.requireNonNull(main, "Required element 'main' is missing");
        String name = obj.getString("name");
        if (name == null) name = id;
        return new PluginDescriptionFile(id, name ,version, main);
    }
}
