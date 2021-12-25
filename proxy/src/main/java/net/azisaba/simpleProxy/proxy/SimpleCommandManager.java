package net.azisaba.simpleProxy.proxy;

import net.azisaba.simpleProxy.api.command.CommandHandler;
import net.azisaba.simpleProxy.api.command.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SimpleCommandManager implements CommandManager {
    private final Map<String, CommandHandler> commands = new HashMap<>();

    @Override
    public void registerCommand(@NotNull String command, @NotNull CommandHandler handler) {
        if (commands.containsKey(command)) {
            throw new IllegalArgumentException("Command conflict: " + command + " (" + commands.get(command).getClass().getTypeName() + ")");
        }
        commands.put(command, handler);
    }

    @Nullable
    @Override
    public CommandHandler getCommandHandler(@NotNull String command) {
        return commands.get(command);
    }

    @Unmodifiable
    @NotNull
    @Override
    public Map<String, CommandHandler> getCommands() {
        return Collections.unmodifiableMap(commands);
    }
}
