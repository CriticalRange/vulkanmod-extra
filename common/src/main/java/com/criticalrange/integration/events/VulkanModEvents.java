package com.criticalrange.integration.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

/**
 * Event system for VulkanMod integration without requiring VulkanMod modifications.
 * These events are triggered by our mixins and provide clean integration points.
 */
public final class VulkanModEvents {

    /**
     * Called when VulkanMod's configuration screen is being initialized.
     * This is the ideal place to add custom pages and options.
     *
     * Return ActionResult.SUCCESS to indicate successful handling.
     * Return ActionResult.PASS to allow other listeners to handle.
     * Return ActionResult.FAIL to prevent further processing.
     */
    public static final Event<ConfigScreenInitCallback> CONFIG_SCREEN_INIT =
        EventFactory.createArrayBacked(ConfigScreenInitCallback.class, listeners -> (screen) -> {
            ActionResult result = ActionResult.PASS;
            for (ConfigScreenInitCallback listener : listeners) {
                ActionResult listenerResult = listener.onConfigScreenInit(screen);
                if (listenerResult != ActionResult.PASS) {
                    result = listenerResult;
                    if (listenerResult == ActionResult.FAIL) {
                        break; // Stop processing on failure
                    }
                }
            }
            return result;
        });

    /**
     * Called when VulkanMod's configuration screen is about to add its default pages.
     * This allows intercepting the page addition process.
     */
    public static final Event<ConfigPagesAddingCallback> CONFIG_PAGES_ADDING =
        EventFactory.createArrayBacked(ConfigPagesAddingCallback.class, listeners -> (screen, pageList) -> {
            ActionResult result = ActionResult.PASS;
            for (ConfigPagesAddingCallback listener : listeners) {
                ActionResult listenerResult = listener.onConfigPagesAdding(screen, pageList);
                if (listenerResult != ActionResult.PASS) {
                    result = listenerResult;
                    if (listenerResult == ActionResult.FAIL) {
                        break;
                    }
                }
            }
            return result;
        });

    /**
     * Called when VulkanMod is about to apply configuration changes.
     * This allows syncing VulkanMod-Extra settings with VulkanMod changes.
     */
    public static final Event<ConfigApplyCallback> CONFIG_APPLY =
        EventFactory.createArrayBacked(ConfigApplyCallback.class, listeners -> (config) -> {
            for (ConfigApplyCallback listener : listeners) {
                listener.onConfigApply(config);
            }
        });

    /**
     * Called when VulkanMod's configuration is written to disk.
     * This allows VulkanMod-Extra to save its own configuration at the same time.
     */
    public static final Event<ConfigSaveCallback> CONFIG_SAVE =
        EventFactory.createArrayBacked(ConfigSaveCallback.class, listeners -> (config) -> {
            for (ConfigSaveCallback listener : listeners) {
                listener.onConfigSave(config);
            }
        });

    /**
     * Called when VulkanMod performs resource reload due to settings changes.
     * This allows coordinating resource reloads across mods.
     */
    public static final Event<ResourceReloadCallback> RESOURCE_RELOAD =
        EventFactory.createArrayBacked(ResourceReloadCallback.class, listeners -> (reason) -> {
            for (ResourceReloadCallback listener : listeners) {
                listener.onResourceReload(reason);
            }
        });

    /**
     * Called when VulkanMod's rendering settings change.
     * This allows adapting VulkanMod-Extra features to VulkanMod's current settings.
     */
    public static final Event<RenderSettingsChangedCallback> RENDER_SETTINGS_CHANGED =
        EventFactory.createArrayBacked(RenderSettingsChangedCallback.class, listeners -> (setting, oldValue, newValue) -> {
            for (RenderSettingsChangedCallback listener : listeners) {
                listener.onRenderSettingChanged(setting, oldValue, newValue);
            }
        });

    /**
     * Called when VulkanMod initializes its Vulkan context.
     * This allows VulkanMod-Extra to initialize Vulkan-dependent features safely.
     */
    public static final Event<VulkanInitCallback> VULKAN_INIT =
        EventFactory.createArrayBacked(VulkanInitCallback.class, listeners -> () -> {
            for (VulkanInitCallback listener : listeners) {
                listener.onVulkanInit();
            }
        });

    /**
     * Called when VulkanMod shuts down its Vulkan context.
     * This allows VulkanMod-Extra to clean up Vulkan resources properly.
     */
    public static final Event<VulkanShutdownCallback> VULKAN_SHUTDOWN =
        EventFactory.createArrayBacked(VulkanShutdownCallback.class, listeners -> () -> {
            for (VulkanShutdownCallback listener : listeners) {
                listener.onVulkanShutdown();
            }
        });

    /**
     * Called when VulkanMod detects or changes the active GPU device.
     * This allows VulkanMod-Extra to adapt to different hardware capabilities.
     */
    public static final Event<DeviceChangedCallback> DEVICE_CHANGED =
        EventFactory.createArrayBacked(DeviceChangedCallback.class, listeners -> (deviceInfo) -> {
            for (DeviceChangedCallback listener : listeners) {
                listener.onDeviceChanged(deviceInfo);
            }
        });

    // Private constructor to prevent instantiation
    private VulkanModEvents() {}
}