package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cloud rendering control mixin for Minecraft 1.21.2
 * Controls cloud rendering and distance based on configuration
 */
@Mixin(WorldRenderer.class)
public class MixinCloudRendering {

    /**
     * Control cloud rendering in Minecraft 1.21.2
     * Uses the renderClouds method which handles all cloud rendering
     */
    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlCloudRendering(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.detailSettings != null) {
            // Cancel cloud rendering when distance is set to 0 or below
            if (VulkanModExtra.CONFIG.detailSettings.cloudDistance <= 0) {
                ci.cancel();
            }
        }
    }
}