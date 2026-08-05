package com.jobpilotai.events;

import com.jobpilotai.logs.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Singleton Internal Event Bus using a Publish-Subscribe pattern.
 */
public class EventBus {

    private static EventBus instance;
    private final Map<Class<? extends IEvent>, List<Consumer<IEvent>>> listeners = new ConcurrentHashMap<>();

    private EventBus() { }

    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    /**
     * Subscribe to a specific event type.
     */
    @SuppressWarnings("unchecked")
    public <T extends IEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                 .add((Consumer<IEvent>) listener);
    }

    /**
     * Publish an event to all subscribers.
     */
    public void publish(IEvent event) {
        if (event == null) return;
        
        AppLogger.info("EventBus: Publishing " + event.getEventName());
        
        List<Consumer<IEvent>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Consumer<IEvent> listener : eventListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    AppLogger.error("Error dispatching event " + event.getEventName(), e);
                }
            }
        }
    }
}
