package com.criticalrange.mixins.vulkanmod;

import com.criticalrange.integration.events.VulkanModEvents;
import com.criticalrange.VulkanModExtra;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simplified mixin that triggers VulkanMod integration events.
 * Only targets the init() method that we confirmed exists in the source.
 * Uses @Pseudo to safely target VulkanMod classes that may not be present.
 */
@Pseudo
@Mixin(targets = "net.vulkanmod.config.gui.VOptionScreen", remap = false)
public class MixinVulkanModEventTrigger {

    /**
     * Trigger CONFIG_SCREEN_INIT event when VulkanMod's config screen initializes
     * Based on source analysis: protected void init() method exists in VOptionScreen
     */
    @Inject(method = "init()V", at = @At("HEAD"))
    protected void onConfigScreenInit(CallbackInfo ci) {
        try {
            Object screen = this;
            ActionResult result = VulkanModEvents.CONFIG_SCREEN_INIT.invoker().onConfigScreenInit(screen);

            if (result == ActionResult.SUCCESS) {
                VulkanModExtra.LOGGER.debug("VulkanMod config screen initialization event handled successfully");
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Error triggering CONFIG_SCREEN_INIT event", e);
        }
    }
}