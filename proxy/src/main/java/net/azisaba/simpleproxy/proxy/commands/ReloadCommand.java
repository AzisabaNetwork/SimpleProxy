package net.azisaba.simpleproxy.proxy.commands;

import net.azisaba.simpleproxy.api.command.CommandHandler;
import net.azisaba.simpleproxy.proxy.ProxyInstance;
import net.azisaba.simpleproxy.proxy.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReloadCommand implements CommandHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void execute(@NotNull List<String> args) {
        ProxyInstance.getInstance().reloadConfig().join();
        LOGGER.info(Util.ANSI_GREEN + "Reloaded configuration" + Util.ANSI_RESET);
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Reloads config.yml and reload listeners.";
    }
}
