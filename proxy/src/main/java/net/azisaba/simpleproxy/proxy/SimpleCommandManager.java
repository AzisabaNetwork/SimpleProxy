package net.azisaba.simpleproxy.proxy;

import net.azisaba.simpleproxy.api.command.CommandHandler;
import net.azisaba.simpleproxy.api.command.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SimpleCommandManager implements CommandManager {
    private final Map<String, CommandHandler> commands = new HashMap<>();

    @Override
    public void registerCommand(@NotNull String command, @NotNull CommandHandler handler) {
        Objects.requireNonNull(command, "command cannot be null");
        Objects.requireNonNull(handler, "handler cannot be null");
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
