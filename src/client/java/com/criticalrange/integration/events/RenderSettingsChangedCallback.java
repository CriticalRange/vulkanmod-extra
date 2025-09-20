package com.criticalrange.integration.events;

/**
 * Callback interface for VulkanMod render settings change events.
 */
@FunctionalInterface
public interface RenderSettingsChangedCallback {
    /**
     * Called when VulkanMod's rendering settings change.
     * This allows VulkanMod-Extra to adapt its features to VulkanMod's current settings.
     *
     * @param setting The name of the setting that changed
     * @param oldValue The previous value of the setting
     * @param newValue The new value of the setting
     */
    void onRenderSettingChanged(String setting, Object oldValue, Object newValue);
}