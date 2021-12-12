package net.azisaba.simpleProxy.commands;

import net.azisaba.simpleProxy.ProxyServer;
import net.azisaba.simpleProxy.util.CommandHandler;
import net.azisaba.simpleProxy.util.InvalidArgumentException;
import net.azisaba.simpleProxy.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class ReloadCommand implements CommandHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void execute(@NotNull List<String> args) throws IOException, InvalidArgumentException {
        ProxyServer.getInstance().reloadConfig();
        LOGGER.info(Util.ANSI_GREEN + "Reloaded configuration" + Util.ANSI_RESET);
    }

    @Override
    public @Nullable String getDescription() {
        return "Reloads config.yml and reload listeners.";
    }
}
