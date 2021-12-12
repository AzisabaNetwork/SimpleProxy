package net.azisaba.simpleProxy;

import net.azisaba.simpleProxy.commands.DebugRuleCommand;
import net.azisaba.simpleProxy.commands.HelpCommand;
import net.azisaba.simpleProxy.commands.ReloadCommand;
import net.azisaba.simpleProxy.commands.StopCommand;
import net.azisaba.simpleProxy.config.ListenerInfo;
import net.azisaba.simpleProxy.config.ProxyConfig;
import net.azisaba.simpleProxy.connection.ConnectionListener;
import net.azisaba.simpleProxy.util.CommandHandler;
import net.azisaba.simpleProxy.util.InvalidArgumentException;
import net.azisaba.simpleProxy.util.Util;
import net.azisaba.simpleProxy.util.Versioning;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProxyServer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static ProxyServer instance;
    private final CommandManager commandManager = new CommandManager();
    private ConnectionListener connectionListener;
    private boolean stopping = false;

    public ProxyServer() {
        instance = this;
    }

    @NotNull
    public static ProxyServer getInstance() {
        return Objects.requireNonNull(instance);
    }

    @NotNull
    public ConnectionListener getConnectionListener() {
        return Objects.requireNonNull(connectionListener);
    }

    @NotNull
    public CommandManager getCommandManager() {
        return commandManager;
    }

    public void start() throws Throwable {
        long start = System.currentTimeMillis();
        LOGGER.info("Loading {} version {}", Versioning.getName(), Versioning.getVersion());
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "Server Shutdown Thread"));
        reloadConfig();
        registerCommands();
        startWaitThread(startConsoleInputThread());
        long time = System.currentTimeMillis() - start;
        LOGGER.info("Proxy initialization done in {} ms", time);
    }

    private void startWaitThread(Thread consoleInputThread) {
        new Thread(() -> {
            while (!stopping) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    LOGGER.warn("Wait thread interrupted");
                    Thread.currentThread().interrupt();
                }
            }
            consoleInputThread.interrupt();
        }, "Wait Thread").start();
    }

    @NotNull
    private Thread startConsoleInputThread() {
        Thread thread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && !stopping) {
                    String s = Util.readLine();
                    handleCommand(s);
                }
            } catch (Throwable e) {
                LOGGER.warn("Console Input Thread crashed", e);
                if (e instanceof RuntimeException) throw (RuntimeException) e;
                throw new RuntimeException(e);
            }
        }, "Console Input Thread");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    public void registerCommands() {
        getCommandManager().registerCommand("help", new HelpCommand());
        getCommandManager().registerCommand("reload", new ReloadCommand());
        getCommandManager().registerCommand("stop", new StopCommand());
        getCommandManager().registerCommand("debugrule", new DebugRuleCommand());
    }

    public void handleCommand(@NotNull String input) {
        if (stopping) return;
        List<String> args = new ArrayList<>(Arrays.asList(input.split("\\s+")));
        if (args.isEmpty()) return;
        String command = args.remove(0);
        CommandHandler handler = commandManager.getCommandHandler(command);
        if (handler == null) {
            LOGGER.info(Util.ANSI_RED + "Unknown command. Type 'help' to show available commands." + Util.ANSI_RESET);
            return;
        }
        try {
            handler.execute(args);
        } catch (Throwable e) {
            if (e instanceof VirtualMachineError) {
                System.gc();
                throw (VirtualMachineError) e;
            }
            LOGGER.error("Exception thrown while processing command '{}'", command, e);
        }
    }

    public void reloadConfig() throws IOException, InvalidArgumentException {
        closeListeners();
        LOGGER.info("Loading config");
        ProxyConfig.init();
        List<ListenerInfo> listeners = ProxyConfig.getValidListeners();
        if (listeners.isEmpty()) {
            LOGGER.warn("No valid listeners defined");
        }
        LOGGER.info("Initializing listeners");
        if (connectionListener == null) {
            connectionListener = new ConnectionListener();
        }
        for (ListenerInfo listener : listeners) {
            connectionListener.listen(listener);
        }
    }

    public void closeListeners() {
        if (connectionListener != null) {
            LOGGER.info("Closing listeners");
            connectionListener.closeFutures();
        }
    }

    public void fullyCloseListeners() {
        if (connectionListener != null) {
            LOGGER.info("Closing listeners");
            connectionListener.close();
            connectionListener = null;
        }
    }

    public void stop() {
        if (stopping) return;
        stopping = true;
        fullyCloseListeners();
        LOGGER.info("Goodbye!");
    }
}
