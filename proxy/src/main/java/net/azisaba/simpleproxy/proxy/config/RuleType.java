package net.azisaba.simpleproxy.proxy.config;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum RuleType {
    ALLOW,
    DENY,
    ;

    @NotNull
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
