package net.azisaba.simpleProxy.api.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

public interface CommandManager {
    void registerCommand(@NotNull String command, @NotNull CommandHandler handler);

    @Nullable
    CommandHandler getCommandHandler(@NotNull String command);

    @Unmodifiable
    @NotNull
    Map<String, CommandHandler> getCommands();
}
