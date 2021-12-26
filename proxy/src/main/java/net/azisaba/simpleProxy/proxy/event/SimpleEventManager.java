package net.azisaba.simpleProxy.proxy.event;

import net.azisaba.simpleProxy.api.event.AbstractEvent;
import net.azisaba.simpleProxy.api.event.Event;
import net.azisaba.simpleProxy.api.event.EventHandler;
import net.azisaba.simpleProxy.api.event.EventManager;
import net.azisaba.simpleProxy.api.event.EventPriority;
import net.azisaba.simpleProxy.api.event.HandlerList;
import net.azisaba.simpleProxy.api.plugin.Plugin;
import net.azisaba.simpleProxy.api.util.ThrowableConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleEventManager implements EventManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConcurrentHashMap<Class<? extends Event>, HandlerList> handlerMap = new ConcurrentHashMap<>();

    private void logInvalidHandler(Method method, String message, Plugin plugin) {
        LOGGER.warn("Invalid EventHandler: {} at {} in mod {}", message, method.toGenericString(), plugin.getId());
    }

    @Override
    public void registerEvents(@NotNull Plugin plugin, @NotNull Object listener) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(listener, "listener cannot be null");
        for (Method method : listener.getClass().getMethods()) {
            if (method.isSynthetic()) continue;
            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            EventHandler eventHandler = method.getAnnotation(EventHandler.class);
            if (method.getParameterCount() != 1) {
                logInvalidHandler(method, "parameter count is not 1", plugin);
                continue;
            }
            if (!method.getReturnType().equals(void.class)) {
                logInvalidHandler(method, "warning: return type is not void (return value will not be used)", plugin);
            }
            if (Modifier.isAbstract(method.getModifiers())) {
                logInvalidHandler(method, "method must not be abstract", plugin);
                continue;
            }
            Class<?> clazz = method.getParameters()[0].getType();
            if (!Event.class.isAssignableFrom(clazz)) {
                logInvalidHandler(method, "parameter type is not assignable from " + Event.class.getCanonicalName(), plugin);
                continue;
            }
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            if (!method.isAccessible()) {
                logInvalidHandler(method, "method is inaccessible", plugin);
                continue;
            }
            Class<? extends Event> eventClass = clazz.asSubclass(Event.class);
            HandlerList handlerList = getHandlerList(eventClass);
            ThrowableConsumer<Event> consumer = isStatic ? event -> method.invoke(null, event) : event -> method.invoke(listener, event);
            handlerList.add(consumer, eventHandler.priority(), listener, plugin);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> void registerEvent(@NotNull Class<T> clazz, @NotNull Plugin plugin, @NotNull EventPriority priority, @NotNull ThrowableConsumer<T> consumer) {
        getHandlerList(clazz).add(event -> consumer.accept((T) event), priority, null, plugin);
    }

    @Override
    public void unregisterEvents(@NotNull Plugin plugin) {
        Objects.requireNonNull(plugin, "mod cannot be null");
        handlerMap.values().forEach(handlerList -> handlerList.remove(plugin));
    }

    @Override
    public void unregisterEvents(@NotNull Object listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        handlerMap.values().forEach(handlerList -> handlerList.remove(listener));
    }

    @Contract("_ -> param1")
    @NotNull
    @Override
    public <T extends Event> T callEvent(@NotNull T event) {
        Objects.requireNonNull(event, "event cannot be null");
        getHandlerList(event.getClass()).fire(event);
        return event;
    }

    @NotNull
    @Override
    public Set<Class<? extends Event>> getKnownEvents() {
        return handlerMap.keySet();
    }

    @Contract(pure = true)
    @NotNull
    @Unmodifiable
    @Override
    public Map<Class<? extends Event>, HandlerList> getHandlerMap() {
        return Collections.unmodifiableMap(handlerMap);
    }

    @NotNull
    @Override
    public HandlerList getHandlerList(@NotNull Class<? extends Event> event) {
        Objects.requireNonNull(event, "event cannot be null");
        if (Modifier.isAbstract(event.getModifiers()) || event.isAnnotationPresent(AbstractEvent.class)) {
            throw new IllegalArgumentException("Cannot get handler list of " + event.getTypeName());
        }
        return handlerMap.computeIfAbsent(event, e -> new HandlerList());
    }
}
