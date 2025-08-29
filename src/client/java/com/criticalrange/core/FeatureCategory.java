package com.criticalrange.core;

/**
 * Categories for organizing features in the GUI and configuration
 */
public enum FeatureCategory {
    ANIMATION("animations", "Animation Settings"),
    PARTICLE("particles", "Particle Settings"),
    RENDER("render", "Render Settings"),
    HUD("hud", "HUD Settings"),
    PERFORMANCE("performance", "Performance Settings"),
    SKY("sky", "Sky & Environment"),
    WEATHER("weather", "Weather Effects"),
    BIOME("biome", "Biome Features"),
    ENTITY("entity", "Entity Rendering"),
    BLOCK("block", "Block Rendering"),
    FOG("fog", "Fog & Visibility"),
    FPS("fps", "FPS Display");

    private final String id;
    private final String displayName;

    FeatureCategory(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
