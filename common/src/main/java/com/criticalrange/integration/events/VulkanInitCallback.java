package com.criticalrange.integration.events;

/**
 * Callback interface for VulkanMod Vulkan initialization events.
 */
@FunctionalInterface
public interface VulkanInitCallback {
    /**
     * Called when VulkanMod initializes its Vulkan context.
     * This allows VulkanMod-Extra to initialize Vulkan-dependent features safely.
     */
    void onVulkanInit();
}