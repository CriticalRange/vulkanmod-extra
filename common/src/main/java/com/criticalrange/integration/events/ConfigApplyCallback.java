package com.criticalrange.integration.events;

/**
 * Callback interface for VulkanMod configuration apply events.
 */
@FunctionalInterface
public interface ConfigApplyCallback {
    /**
     * Called when VulkanMod is applying configuration changes.
     * This allows VulkanMod-Extra to synchronize its settings with VulkanMod changes.
     *
     * @param config The VulkanMod configuration object being applied
     */
    void onConfigApply(Object config);
}