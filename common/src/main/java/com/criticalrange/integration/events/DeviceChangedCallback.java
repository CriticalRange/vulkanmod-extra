package com.criticalrange.integration.events;

/**
 * Callback interface for VulkanMod device change events.
 */
@FunctionalInterface
public interface DeviceChangedCallback {
    /**
     * Called when VulkanMod detects or changes the active GPU device.
     * This allows VulkanMod-Extra to adapt to different hardware capabilities.
     *
     * @param deviceInfo Information about the new device (device name, capabilities, etc.)
     */
    void onDeviceChanged(Object deviceInfo);
}