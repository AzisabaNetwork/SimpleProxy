package net.azisaba.simpleProxy.api.util;

import org.jetbrains.annotations.NotNull;

public interface ThrowableConsumer<T> {
    void accept(@NotNull T t) throws Throwable;
}
