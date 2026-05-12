package com.harriswebstudio.router.event;

import com.harriswebstudio.router.event.annotation.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandler.class);

    private final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<Consumer<Event>>> map = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public void registerListener(Object listenerObject) {
        Class<?> clazz = listenerObject.getClass();
        boolean registered = false;

        for (Method method : clazz.getMethods()) {
            if (!method.isAnnotationPresent(Listener.class))
                continue;

            Class<?>[] params = method.getParameterTypes();

            if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
                LOGGER.warn(
                        "Method {}.{} is annotated with @Listener but does not accept a single Event parameter – skipped",
                        clazz.getName(), method.getName()
                );
                continue;
            }

            Class<? extends Event> eventClass = (Class<? extends Event>) params[0];
            method.setAccessible(true);

            Consumer<Event> consumer = event -> {
                try {
                    method.invoke(listenerObject, event);
                } catch (Exception e) {
                    LOGGER.error("Error dispatching event {} to {}.{}", event.getClass().getSimpleName(), clazz.getName(), method.getName(), e);
                }
            };

            map.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(consumer);
            registered = true;
        }

        if (!registered) {
            LOGGER.warn("No valid @Listener methods found on {}", clazz.getName());
        }
    }

    public <T extends Event> T callEvent(T event) {
        try {
            for (Class<?> clazz = event.getClass(); clazz != null && Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
                CopyOnWriteArrayList<Consumer<Event>> list = map.get(clazz);

                if (list != null) {
                    for (Consumer<Event> consumer : list) {
                        if (event instanceof Cancellable cancellable && cancellable.isCancelled())
                            break;

                        consumer.accept(event);
                    }
                }

                if (event instanceof Cancellable cancellable && cancellable.isCancelled())
                    break;
            }
        } catch (Exception exception) {
            LOGGER.error("Error calling event {}", event.getClass().getSimpleName(), exception);
        }
        return event;
    }

}