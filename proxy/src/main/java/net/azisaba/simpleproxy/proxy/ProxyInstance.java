package net.azisaba.simpleproxy.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ResourceLeakDetector;
import net.azisaba.simpleproxy.api.ProxyServer;
import net.azisaba.simpleproxy.api.ProxyServerHolder;
import net.azisaba.simpleproxy.api.config.ListenerInfo;
import net.azisaba.simpleproxy.api.config.ProxyConfig;
import net.azisaba.simpleproxy.api.config.ServerInfo;
import net.azisaba.simpleproxy.api.event.proxy.ProxyInitializeEvent;
import net.azisaba.simpleproxy.api.event.proxy.ProxyReloadEvent;
import net.azisaba.simpleproxy.api.event.proxy.ProxyShutdownEvent;
import net.azisaba.simpleproxy.api.yaml.YamlObject;
import net.azisaba.simpleproxy.proxy.commands.DebugRuleCommand;
import net.azisaba.simpleproxy.proxy.commands.HelpCommand;
import net.azisaba.simpleproxy.proxy.commands.ReloadCommand;
import net.azisaba.simpleproxy.proxy.commands.StopCommand;
import net.azisaba.simpleproxy.proxy.config.ListenerInfoImpl;
import net.azisaba.simpleproxy.proxy.config.ProxyConfigImpl;
import net.azisaba.simpleproxy.proxy.config.ProxyConfigInstance;
import net.azisaba.simpleproxy.proxy.config.ServerInfoImpl;
import net.azisaba.simpleproxy.proxy.connection.ConnectionListener;
import net.azisaba.simpleproxy.api.command.CommandHandler;
import net.azisaba.simpleproxy.api.command.InvalidArgumentException;
import net.azisaba.simpleproxy.proxy.connection.MessageForwarder;
import net.azisaba.simpleproxy.proxy.event.SimpleEventManager;
import net.azisaba.simpleproxy.proxy.plugin.SimplePluginLoader;
import net.azisaba.simpleproxy.proxy.util.Util;
import net.azisaba.simpleproxy.proxy.util.Versioning;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
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
    private final ProxyConfig proxyConfig = new ProxyConfigImpl();
    private ConnectionListener connectionListener;
    private boolean stopping = false;

    public ProxyInstance() {
        ProxyServerHolder.setProxyServer(this);
        instance = this;
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
        if (pluginLoader.isEnabled()) {
            LOGGER.info("Loading plugins");
            try {
                pluginLoader.loadPlugins();
            } catch (IOException e) {
                LOGGER.warn("Failed to load plugins", e);
            }
            LOGGER.info("Loaded {} plugins", pluginLoader.getPlugins().size());
        }
        startWaitThread(startConsoleInputThread());
        long time = System.currentTimeMillis() - start;
        LOGGER.info("Proxy initialization done in {} ms", time);
        ProxyInitializeEvent.INSTANCE.callEvent();
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
            try {
                closeListeners();
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                LOGGER.error(e);
                Thread.currentThread().interrupt();
            }
            LOGGER.info("Loading config");
            try {
                ProxyConfigInstance.init();
            } catch (IOException | InvalidArgumentException e) {
                LOGGER.fatal("Could not load proxy configuration", e);
                throw new RuntimeException(e);
            }
            if (!pluginLoader.isEnabled()) LOGGER.info("Plugin loader is disabled");
            if (!eventManager.isEnabled()) LOGGER.info("Event manager is disabled");
            ResourceLeakDetector.Level resourceLeakDetectorLevel = ResourceLeakDetector.Level.SIMPLE;
            if (ProxyConfigInstance.debug) {
                LOGGER.info("Debug mode is enabled");
                resourceLeakDetectorLevel = ResourceLeakDetector.Level.PARANOID;
            } else if (ProxyConfigInstance.verbose) {
                LOGGER.info("Verbose mode is enabled");
                resourceLeakDetectorLevel = ResourceLeakDetector.Level.ADVANCED;
            }
            LOGGER.info("Resource leak detector level is set to {}", resourceLeakDetectorLevel);
            ResourceLeakDetector.setLevel(resourceLeakDetectorLevel);
            List<ListenerInfoImpl> listeners = ProxyConfigInstance.listeners;
            if (listeners.isEmpty()) {
                LOGGER.warn("No listeners defined");
            }
            LOGGER.info("Initializing listeners");
            if (connectionListener == null) {
                connectionListener = new ConnectionListener();
            }
            for (ListenerInfoImpl listener : listeners) {
                try {
                    connectionListener.listen(listener);
                } catch (Exception e) {
                    LOGGER.warn("Failed to listen port for {}:{}", listener.getHost(), listener.getListenPort(), e);
                }
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

    @NotNull
    @Override
    public ProxyConfig getConfig() {
        return proxyConfig;
    }

    public void closeListeners() throws ExecutionException, InterruptedException, TimeoutException {
        if (connectionListener != null) {
            LOGGER.info("Closing listeners");
            connectionListener.closeFutures().get(3, TimeUnit.MINUTES);
        }
    }

    public void fullyCloseListeners() throws ExecutionException, InterruptedException, TimeoutException {
        if (connectionListener != null) {
            LOGGER.info("Closing listeners");
            connectionListener.close().get(3, TimeUnit.MINUTES);
            connectionListener = null;
        }
    }

    @Override
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
        try {
            fullyCloseListeners();
            LOGGER.info("Shutting down executor");
            worker.shutdownNow();
            //noinspection ResultOfMethodCallIgnored
            worker.awaitTermination(3, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error(e);
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Goodbye!");
    }

    @Override
    public @NotNull Unsafe unsafe() {
        return TheUnsafe.INSTANCE;
    }

    public static class TheUnsafe implements Unsafe {
        private static final Unsafe INSTANCE = new TheUnsafe();

        @Override
        public @NotNull ChannelInboundHandlerAdapter createMessageForwarder(@NotNull Channel ch, @NotNull ListenerInfo listenerInfo, @NotNull ServerInfo remoteServerInfo) {
            return new MessageForwarder(ch, listenerInfo, remoteServerInfo);
        }

        @Override
        public @NotNull ListenerInfo createListenerInfo(@NotNull YamlObject obj) {
            return new ListenerInfoImpl(obj);
        }

        @Override
        public @NotNull ServerInfo createServerInfo(@NotNull YamlObject obj) {
            return new ServerInfoImpl(obj);
        }
    }
}
