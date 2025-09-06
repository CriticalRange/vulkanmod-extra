package com.criticalrange.mixin.sky;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sky rendering control mixin
 * Controls sky rendering based on user preferences
 */
@Mixin(LevelRenderer.class)
public class MixinSkyRenderer {

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlSkyRendering(CallbackInfo ci) {
        // Check if sky rendering is disabled
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.detailSettings.sky) {
            // Sky rendering is disabled - cancel the entire sky rendering
            ci.cancel();
            return;
        }

        // Additional sky rendering optimizations:
        // - Sky geometry optimizations
        // - Star field optimizations  
        // - Sky color calculation optimizations
    }
}
