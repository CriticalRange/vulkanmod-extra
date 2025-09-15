package com.criticalrange.core.events;

/**
 * Enumeration of all feature event types
 */
public enum FeatureEventType {
    // Feature lifecycle events
    FEATURE_INITIALIZED("feature.initialized"),
    FEATURE_ENABLED("feature.enabled"),
    FEATURE_DISABLED("feature.disabled"),
    FEATURE_ERROR("feature.error"),
    FEATURE_RECOVERED("feature.recovered"),

    // Configuration events
    CONFIG_CHANGED("config.changed"),
    CONFIG_RELOADED("config.reloaded"),
    CONFIG_SAVED("config.saved"),

    // Render events
    RENDER_TICK("render.tick"),
    RENDER_SETUP("render.setup"),
    RENDER_CLEANUP("render.cleanup"),

    // World events
    WORLD_LOAD("world.load"),
    WORLD_UNLOAD("world.unload"),
    CHUNK_LOAD("chunk.load"),
    CHUNK_UNLOAD("chunk.unload"),

    // Player events
    PLAYER_JOIN("player.join"),
    PLAYER_LEAVE("player.leave"),
    PLAYER_RESPAWN("player.respawn"),

    // Performance events
    PERFORMANCE_WARNING("performance.warning"),
    MEMORY_LOW("memory.low"),
    FPS_DROP("fps.drop"),

    // System events
    SHUTDOWN("system.shutdown"),
    RELOAD_RESOURCES("reload.resources"),
    DEBUG_MODE_CHANGED("debug.mode.changed");

    private final String eventName;

    FeatureEventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public static FeatureEventType fromEventName(String eventName) {
        for (FeatureEventType type : values()) {
            if (type.eventName.equals(eventName)) {
                return type;
            }
        }
        return null;
    }
}