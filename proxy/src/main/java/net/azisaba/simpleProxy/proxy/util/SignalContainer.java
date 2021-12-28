package net.azisaba.simpleProxy.proxy.util;

import org.jetbrains.annotations.NotNull;

public class SignalContainer {
    private final String name;
    private final int number;

    public SignalContainer(@NotNull String name, int number) {
        this.name = name;
        this.number = number;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }
}
