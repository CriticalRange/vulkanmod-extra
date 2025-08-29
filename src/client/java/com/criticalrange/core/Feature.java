package com.criticalrange.core;

import net.minecraft.client.Minecraft;

/**
 * Base interface for all VulkanMod Extra features.
 * Provides a clean abstraction for feature lifecycle management.
 */
public interface Feature {

    /**
     * Get the unique identifier for this feature
     */
    String getId();

    /**
     * Get the display name for this feature
     */
    String getName();

    /**
     * Initialize the feature. Called once during client initialization.
     */
    default void initialize(Minecraft minecraft) {
        // Default implementation - override if needed
    }

    /**
     * Check if this feature is enabled
     */
    boolean isEnabled();

    /**
     * Enable or disable this feature
     */
    void setEnabled(boolean enabled);

    /**
     * Called when the feature is enabled
     */
    default void onEnable() {
        // Default implementation - override if needed
    }

    /**
     * Called when the feature is disabled
     */
    default void onDisable() {
        // Default implementation - override if needed
    }

    /**
     * Called every client tick
     */
    default void onTick(Minecraft minecraft) {
        // Default implementation - override if needed
    }

    /**
     * Get the category this feature belongs to
     */
    FeatureCategory getCategory();

    /**
     * Get a description of what this feature does
     */
    default String getDescription() {
        return "No description available";
    }
}
