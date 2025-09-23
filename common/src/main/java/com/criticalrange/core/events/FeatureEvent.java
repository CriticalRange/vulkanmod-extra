package com.criticalrange.core.events;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Base event class for all feature system events
 */
public class FeatureEvent {
    private final String eventType;
    private final UUID eventId;
    private final long timestamp;
    private final Map<String, Object> data;

    public FeatureEvent(String eventType) {
        this.eventType = eventType;
        this.eventId = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
        this.data = new HashMap<>();
    }

    public FeatureEvent(String eventType, Map<String, Object> data) {
        this(eventType);
        if (data != null) {
            this.data.putAll(data);
        }
    }

    public String getEventType() {
        return eventType;
    }

    public UUID getEventId() {
        return eventId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }

    public <T> T getData(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    public void putData(String key, Object value) {
        data.put(key, value);
    }

    public boolean hasData(String key) {
        return data.containsKey(key);
    }

    @Override
    public String toString() {
        return String.format("FeatureEvent[type=%s, id=%s, timestamp=%d, data=%s]",
                           eventType, eventId, timestamp, data);
    }
}