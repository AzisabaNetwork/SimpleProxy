package net.azisaba.simpleProxy.api.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommandHandler {
    void execute(@NotNull List<String> args) throws Exception;

    @Nullable
    default String getDescription() {
        return null;
    }
}
