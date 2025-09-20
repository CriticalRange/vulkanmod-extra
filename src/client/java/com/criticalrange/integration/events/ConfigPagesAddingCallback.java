package com.criticalrange.integration.events;

import net.minecraft.util.ActionResult;
import java.util.List;

/**
 * Callback interface for VulkanMod configuration page addition events.
 */
@FunctionalInterface
public interface ConfigPagesAddingCallback {
    /**
     * Called when VulkanMod is about to add its default configuration pages.
     * This allows mods to inject their own pages into the configuration screen.
     *
     * @param screen The VulkanMod configuration screen instance
     * @param pageList The list of pages being added (may be modifiable)
     * @return ActionResult indicating how the event was handled:
     *         - SUCCESS: Event handled successfully, continue with other listeners
     *         - PASS: Event not handled, allow other listeners to process
     *         - FAIL: Event handling failed, stop processing other listeners
     */
    ActionResult onConfigPagesAdding(Object screen, List<?> pageList);
}