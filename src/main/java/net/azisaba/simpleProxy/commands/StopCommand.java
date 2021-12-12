package net.azisaba.simpleProxy.commands;

import net.azisaba.simpleProxy.ProxyServer;
import net.azisaba.simpleProxy.util.CommandHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StopCommand implements CommandHandler {
    @Override
    public void execute(@NotNull List<String> args) {
        ProxyServer.getInstance().stop();
    }

    @Override
    public @Nullable String getDescription() {
        return "Closes all listeners and exits application.";
    }
}
