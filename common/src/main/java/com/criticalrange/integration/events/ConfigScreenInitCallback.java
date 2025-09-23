package com.criticalrange.integration.events;

import net.minecraft.util.ActionResult;

/**
 * Callback interface for VulkanMod configuration screen initialization events.
 */
@FunctionalInterface
public interface ConfigScreenInitCallback {
    /**
     * Called when VulkanMod's configuration screen is being initialized.
     * This is the ideal time to add custom pages and configure integration.
     *
     * @param screen The VulkanMod configuration screen instance
     * @return ActionResult indicating how the event was handled:
     *         - SUCCESS: Event handled successfully, continue with other listeners
     *         - PASS: Event not handled, allow other listeners to process
     *         - FAIL: Event handling failed, stop processing other listeners
     */
    ActionResult onConfigScreenInit(Object screen);
}