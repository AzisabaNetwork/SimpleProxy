package net.azisaba.simpleProxy;

import net.azisaba.simpleProxy.util.CommandHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final Map<String, CommandHandler> commands = new HashMap<>();

    public void registerCommand(@NotNull String command, @NotNull CommandHandler handler) {
        if (commands.containsKey(command)) {
            throw new IllegalArgumentException("Command conflict: " + command + " (" + commands.get(command).getClass().getTypeName() + ")");
        }
        commands.put(command, handler);
    }

    @Nullable
    public CommandHandler getCommandHandler(@NotNull String command) {
        return commands.get(command);
    }

    @NotNull
    public Map<String, CommandHandler> getCommands() {
        return Collections.unmodifiableMap(commands);
    }
}
