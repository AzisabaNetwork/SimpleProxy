package net.azisaba.simpleProxy.proxy;

import net.azisaba.simpleProxy.api.ProxyServer;
import net.azisaba.simpleProxy.api.ProxyServerHolder;
import net.azisaba.simpleProxy.api.event.proxy.ProxyReloadEvent;
import net.azisaba.simpleProxy.api.event.proxy.ProxyShutdownEvent;
import net.azisaba.simpleProxy.proxy.commands.DebugRuleCommand;
import net.azisaba.simpleProxy.proxy.commands.HelpCommand;
import net.azisaba.simpleProxy.proxy.commands.ReloadCommand;
import net.azisaba.simpleProxy.proxy.commands.StopCommand;
import net.azisaba.simpleProxy.proxy.config.ListenerInfo;
import net.azisaba.simpleProxy.proxy.config.ProxyConfig;
import net.azisaba.simpleProxy.proxy.connection.ConnectionListener;
import net.azisaba.simpleProxy.api.command.CommandHandler;
import net.azisaba.simpleProxy.api.command.InvalidArgumentException;
import net.azisaba.simpleProxy.proxy.event.SimpleEventManager;
import net.azisaba.simpleProxy.proxy.plugin.SimplePluginLoader;
import net.azisaba.simpleProxy.proxy.util.Util;
import net.azisaba.simpleProxy.proxy.util.Versioning;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ProxyInstance implements ProxyServer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static ProxyInstance instance;
    private final AtomicLong workerId = new AtomicLong();
    private final ExecutorService worker = Executors.newCachedThreadPool(r -> new Thread(() -> {
        LOGGER.debug("Thread {} started", Thread.currentThread().getName());
        try {
            r.run();
        } catch (Throwable t) {
            LOGGER.warn("Thread {} died", Thread.currentThread().getName(), t);
            if (t instanceof Error) throw t;
        } finally {
            LOGGER.debug("Thread {} shutdown", Thread.currentThread().getName());
        }
    }, "Worker-Main-" + workerId.incrementAndGet()));
    private final Versioning version = new Versioning();
    private final SimpleCommandManager commandManager = new SimpleCommandManager();
    private final SimpleEventManager eventManager = new SimpleEventManager();
    private final SimplePluginLoader pluginLoader = new SimplePluginLoader();
    private ConnectionListener connectionListener;
    private boolean stopping = false;

    public ProxyInstance() {
        instance = this;
        ProxyServerHolder.setProxyServer(this);
    }

    @NotNull
    public static ProxyInstance getInstance() {
        return Objects.requireNonNull(instance);
    }

    @NotNull
    public ConnectionListener getConnectionListener() {
        return Objects.requireNonNull(connectionListener);
    }

    @NotNull
    @Override
    public SimpleCommandManager getCommandManager() {
        return commandManager;
    }

    public void start() {
        long start = System.currentTimeMillis();
        LOGGER.info("Loading {} version {}", version.getName(), version.getVersion());
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "Server Shutdown Thread"));
        reloadConfig().join();
        registerCommands();
        LOGGER.info("Loading plugins");
        try {
            pluginLoader.loadPlugins();
        } catch (IOException e) {
            LOGGER.warn("Failed to load plugins", e);
        }
        LOGGER.info("Loaded {} plugins", pluginLoader.getPlugins().size());
        startWaitThread(startConsoleInputThread());
        long time = System.currentTimeMillis() - start;
        LOGGER.info("Proxy initialization done in {} ms", time);
    }

    private void startWaitThread(Thread consoleInputThread) {
        worker.execute(() -> {
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
        });
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
        if (command.isEmpty()) return;
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

    @NotNull
    @Override
    public CompletableFuture<Void> reloadConfig() {
        return CompletableFuture.runAsync(() -> {
            closeListeners();
            LOGGER.info("Loading config");
            try {
                ProxyConfig.init();
            } catch (IOException | InvalidArgumentException e) {
                LOGGER.fatal("Could not load proxy configuration", e);
                throw new RuntimeException(e);
            }
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
            ProxyReloadEvent.INSTANCE.callEvent();
        }, worker);
    }

    @NotNull
    @Override
    public Versioning getVersion() {
        return version;
    }

    @NotNull
    @Override
    public SimpleEventManager getEventManager() {
        return eventManager;
    }

    @NotNull
    @Override
    public SimplePluginLoader getPluginLoader() {
        return pluginLoader;
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
        ProxyShutdownEvent.INSTANCE.callEvent();
        LOGGER.info("Disabling plugins");
        try {
            pluginLoader.close();
        } catch (IOException e) {
            LOGGER.warn("Failed to close plugin loader", e);
        }
        fullyCloseListeners();
        LOGGER.info("Shutting down executor");
        worker.shutdownNow();
        try {
            //noinspection ResultOfMethodCallIgnored
            worker.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Goodbye!");
    }
}
