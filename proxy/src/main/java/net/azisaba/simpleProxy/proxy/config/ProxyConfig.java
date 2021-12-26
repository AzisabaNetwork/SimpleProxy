package net.azisaba.simpleProxy.proxy.config;

import io.netty.channel.epoll.Epoll;
import net.azisaba.simpleProxy.api.command.InvalidArgumentException;
import net.azisaba.simpleProxy.api.yaml.YamlArray;
import net.azisaba.simpleProxy.api.yaml.YamlConfiguration;
import net.azisaba.simpleProxy.api.yaml.YamlObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProxyConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<Field> FIELDS = new ArrayList<>();
    private static YamlObject config;

    public static void init() throws IOException, ClassCastException, InvalidArgumentException {
        reset();
        File file = new File("./config.yml");
        boolean shouldSave = !file.exists();
        if (!file.exists() && !file.createNewFile()) {
            LOGGER.warn("Failed to create " + file.getAbsolutePath());
        }
        config = new YamlConfiguration(file).asObject();
        for (Field field : FIELDS) {
            String serializedName = field.getAnnotation(SerializedName.class).value();
            try {
                Object def = field.get(null);
                field.set(null, config.get(serializedName, def));
            } catch (ReflectiveOperationException ex) {
                LOGGER.warn("Failed to get or set field '{}' (serialized name: {})", field.getName(), serializedName, ex);
            }
        }
        YamlArray listeners = config.getArray("listeners");
        if (listeners != null) {
            listeners.<Map<String, Object>>forEachAsType(obj -> {
                if (obj == null) return;
                ProxyConfig.listeners.add(new ListenerInfo(new YamlObject(YamlConfiguration.DEFAULT, obj)));
            });
        }
        YamlArray rules = config.getArray("rules");
        if (rules != null) {
            ProxyConfig.rules.read(rules);
        }
        if (shouldSave) save();
    }

    public static void save() throws IOException {
        if (config == null) throw new RuntimeException("#init was not called");
        for (Field field : FIELDS) {
            String serializedName = field.getAnnotation(SerializedName.class).value();
            try {
                Object value = field.get(null);
                config.setNullable(serializedName, value);
            } catch (ReflectiveOperationException ex) {
                LOGGER.warn("Failed to get field '{}' (serialized name: {})", field.getName(), serializedName, ex);
            }
        }
        config.save(new File("./config.yml"));
    }

    public static void reset() {
        listeners.clear();
        rules.clear();
        epoll = true;
        debug = false;
    }

    public static List<ListenerInfo> listeners = new ArrayList<>();

    @NotNull
    public static List<ListenerInfo> getValidListeners() {
        return listeners.stream().filter(info -> !info.getServers().isEmpty()).collect(Collectors.toList());
    }

    public static RuleSet rules = new RuleSet();

    @SerializedName("epoll")
    public static boolean epoll = true;

    @SerializedName("debug")
    public static boolean debug = false;

    @SerializedName("verbose")
    public static boolean verbose = true;

    @SerializedName("disablePlugins")
    public static boolean disablePlugins = false;

    public static boolean isEpoll() {
        return epoll && Epoll.isAvailable();
    }

    static {
        for (Field field : ProxyConfig.class.getFields()) {
            if (!Modifier.isPublic(field.getModifiers())) continue;
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (field.isSynthetic()) continue;
            SerializedName serializedNameAnnotation = field.getAnnotation(SerializedName.class);
            if (serializedNameAnnotation == null) continue;
            String serializedName = serializedNameAnnotation.value();
            if (serializedName.equals("")) continue;
            FIELDS.add(field);
        }
    }
}
