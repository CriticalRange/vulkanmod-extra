package com.criticalrange.integration.events;

/**
 * Callback interface for VulkanMod configuration save events.
 */
@FunctionalInterface
public interface ConfigSaveCallback {
    /**
     * Called when VulkanMod is saving its configuration to disk.
     * This allows VulkanMod-Extra to save its own configuration at the same time.
     *
     * @param config The VulkanMod configuration object being saved
     */
    void onConfigSave(Object config);
}