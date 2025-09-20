package com.criticalrange.integration.events;

/**
 * Callback interface for VulkanMod resource reload events.
 */
@FunctionalInterface
public interface ResourceReloadCallback {
    /**
     * Called when VulkanMod performs a resource reload due to settings changes.
     * This allows coordinating resource reloads across mods.
     *
     * @param reason The reason for the resource reload (e.g., "animation_settings", "render_settings")
     */
    void onResourceReload(String reason);
}