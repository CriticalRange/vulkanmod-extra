package com.criticalrange.integration.events;

/**
 * Callback interface for VulkanMod Vulkan shutdown events.
 */
@FunctionalInterface
public interface VulkanShutdownCallback {
    /**
     * Called when VulkanMod shuts down its Vulkan context.
     * This allows VulkanMod-Extra to clean up Vulkan resources properly.
     */
    void onVulkanShutdown();
}