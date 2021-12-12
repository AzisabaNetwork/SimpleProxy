package net.azisaba.simpleProxy.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class Versioning {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String name;
    private static final String version;

    static {
        String n = "SimpleProxy";
        String v = "unknown";
        try {
            InputStream in = Versioning.class.getResourceAsStream("/version.properties");
            Properties properties = new Properties();
            properties.load(in);
            n = properties.getProperty("name");
            v = properties.getProperty("version");
            if (n == null || "@name@".equals(n)) n = "SimpleProxy";
            if (v == null || "@version@".equals(v)) v = "unknown";
        } catch (IOException e) {
            LOGGER.warn("Failed to read version.properties", e);
        }
        name = n;
        version = v;
    }

    @NotNull
    public static String getName() {
        return Objects.requireNonNull(name);
    }

    @NotNull
    public static String getVersion() {
        return Objects.requireNonNull(version);
    }
}
