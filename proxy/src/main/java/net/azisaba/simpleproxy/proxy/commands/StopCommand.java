package net.azisaba.simpleproxy.proxy.commands;

import net.azisaba.simpleproxy.proxy.ProxyInstance;
import net.azisaba.simpleproxy.api.command.CommandHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StopCommand implements CommandHandler {
    @Override
    public void execute(@NotNull List<String> args) {
        ProxyInstance.getInstance().stop();
    }

    @Override
    public @Nullable String getDescription() {
        return "Closes all listeners and exits application.";
    }
}
