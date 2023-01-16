package net.azisaba.simpleproxy.proxy.commands;

import net.azisaba.simpleproxy.api.command.CommandHandler;
import net.azisaba.simpleproxy.proxy.ProxyInstance;
import net.azisaba.simpleproxy.proxy.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HelpCommand implements CommandHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void execute(@NotNull List<String> args) {
        LOGGER.info(Util.ANSI_YELLOW + "----- Available commands -----" + Util.ANSI_RESET);
        ProxyInstance.getInstance().getCommandManager().getCommands().forEach((command, handler) ->
                LOGGER.info(Util.ANSI_CYAN + " - {}: {}{}" + Util.ANSI_RESET, command, Util.ANSI_GREEN, handler.getDescription())
        );
    }

    @Override
    public @Nullable String getDescription() {
        return "You're looking at it right now!";
    }
}
