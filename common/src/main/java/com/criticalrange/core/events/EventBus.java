package com.criticalrange.core.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Central event bus for feature system communication
 * Provides event-driven architecture with prioritized handlers and error recovery
 */
public class EventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra Event Bus");
    private static final EventBus INSTANCE = new EventBus();

    // Event handlers by event type
    private final Map<String, Set<FeatureEventHandler>> handlers = new ConcurrentHashMap<>();

    // Priority-based handlers (higher number = higher priority)
    private final Map<String, TreeMap<Integer, Set<FeatureEventHandler>>> priorityHandlers = new ConcurrentHashMap<>();

    // Event statistics
    private final AtomicLong eventsProcessed = new AtomicLong(0);
    private final AtomicLong eventsFailed = new AtomicLong(0);

    // Event history for debugging
    private final Queue<FeatureEvent> eventHistory = new ConcurrentLinkedQueue<>();
    private static final int MAX_HISTORY_SIZE = 1000;

    private EventBus() {
        // Private constructor for singleton
    }

    public static EventBus getInstance() {
        return INSTANCE;
    }

    /**
     * Register an event handler for a specific event type
     */
    public void register(String eventType, FeatureEventHandler handler) {
        register(eventType, handler, 0); // Default priority
    }

    /**
     * Register an event handler with a specific priority
     */
    public void register(String eventType, FeatureEventHandler handler, int priority) {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(handler, "Handler cannot be null");

        // Add to regular handlers
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>()).add(handler);

        // Add to priority handlers
        priorityHandlers.computeIfAbsent(eventType, k -> new TreeMap<>(Collections.reverseOrder()))
                        .computeIfAbsent(priority, p -> new CopyOnWriteArraySet<>())
                        .add(handler);

        LOGGER.debug("Registered handler for event type '{}' with priority {}", eventType, priority);
    }

    /**
     * Unregister an event handler
     */
    public void unregister(String eventType, FeatureEventHandler handler) {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(handler, "Handler cannot be null");

        // Remove from regular handlers
        Set<FeatureEventHandler> handlerSet = handlers.get(eventType);
        if (handlerSet != null) {
            handlerSet.remove(handler);
        }

        // Remove from priority handlers
        Map<Integer, Set<FeatureEventHandler>> priorityMap = priorityHandlers.get(eventType);
        if (priorityMap != null) {
            priorityMap.values().forEach(set -> set.remove(handler));
            // Clean up empty priority levels
            priorityMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }

        LOGGER.debug("Unregistered handler for event type '{}'", eventType);
    }

    /**
     * Post an event to the bus
     */
    public void post(FeatureEvent event) {
        Objects.requireNonNull(event, "Event cannot be null");

        try {
            // Add to history
            eventHistory.offer(event);
            if (eventHistory.size() > MAX_HISTORY_SIZE) {
                eventHistory.poll();
            }

            // Get handlers for this event type
            Set<FeatureEventHandler> eventHandlers = handlers.getOrDefault(event.getEventType(), Collections.emptySet());

            // Get priority handlers
            Map<Integer, Set<FeatureEventHandler>> priorityMap = priorityHandlers.getOrDefault(event.getEventType(), new TreeMap<>(Collections.reverseOrder()));

            // Process handlers by priority
            boolean eventHandled = false;
            for (Map.Entry<Integer, Set<FeatureEventHandler>> entry : priorityMap.entrySet()) {
                for (FeatureEventHandler handler : entry.getValue()) {
                    try {
                        if (handler.handle(event)) {
                            eventHandled = true;
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error in event handler for event type '{}' with priority {}: {}",
                                   event.getEventType(), entry.getKey(), e.getMessage(), e);
                        eventsFailed.incrementAndGet();
                    }
                }
            }

            // Process remaining handlers (those without specific priority)
            for (FeatureEventHandler handler : eventHandlers) {
                try {
                    if (handler.handle(event)) {
                        eventHandled = true;
                    }
                } catch (Exception e) {
                    LOGGER.error("Error in event handler for event type '{}': {}",
                               event.getEventType(), e.getMessage(), e);
                    eventsFailed.incrementAndGet();
                }
            }

            eventsProcessed.incrementAndGet();

            if (!eventHandled && LOGGER.isDebugEnabled()) {
                LOGGER.debug("Event '{}' was not handled by any handlers", event.getEventType());
            }

        } catch (Exception e) {
            LOGGER.error("Critical error posting event '{}': {}", event.getEventType(), e.getMessage(), e);
            eventsFailed.incrementAndGet();
        }
    }

    /**
     * Post a simple event with just the event type
     */
    public void post(String eventType) {
        post(new FeatureEvent(eventType));
    }

    /**
     * Post an event with data
     */
    public void post(String eventType, Map<String, Object> data) {
        post(new FeatureEvent(eventType, data));
    }

    /**
     * Post a feature lifecycle event
     */
    public void postFeatureEvent(String eventType, String featureId, Object additionalData) {
        Map<String, Object> data = new HashMap<>();
        data.put("featureId", featureId);
        if (additionalData != null) {
            if (additionalData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapData = (Map<String, Object>) additionalData;
                data.putAll(mapData);
            } else {
                data.put("data", additionalData);
            }
        }
        post(eventType, data);
    }

    /**
     * Get event statistics
     */
    public EventStatistics getStatistics() {
        return new EventStatistics(
            eventsProcessed.get(),
            eventsFailed.get(),
            eventHistory.size()
        );
    }

    /**
     * Get recent event history
     */
    public List<FeatureEvent> getRecentEvents(int count) {
        return eventHistory.stream()
                          .limit(Math.min(count, MAX_HISTORY_SIZE))
                          .toList();
    }

    /**
     * Clear event history
     */
    public void clearHistory() {
        eventHistory.clear();
    }

    /**
     * Get the number of registered handlers for an event type
     */
    public int getHandlerCount(String eventType) {
        Set<FeatureEventHandler> handlerSet = handlers.get(eventType);
        return handlerSet != null ? handlerSet.size() : 0;
    }

    /**
     * Get all registered event types
     */
    public Set<String> getRegisteredEventTypes() {
        return new HashSet<>(handlers.keySet());
    }

    /**
     * Event statistics record
     */
    public record EventStatistics(long eventsProcessed, long eventsFailed, int historySize) {
        public double getSuccessRate() {
            return eventsProcessed > 0 ? (eventsProcessed - eventsFailed) * 100.0 / eventsProcessed : 100.0;
        }
    }
}