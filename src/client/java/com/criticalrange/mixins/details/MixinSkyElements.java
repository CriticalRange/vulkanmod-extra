package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls sky element rendering (sky, sun, moon, stars)
 * 
 * CURRENT LIMITATION: This mixin currently disables the entire sky (gradient, sun, moon, stars)
 * when the sky option is turned off. This is a temporary approach until more granular 
 * injection points can be identified for targeting only the sky gradient.
 * 
 * The ideal behavior would be to disable only the sky gradient/background color
 * while preserving celestial objects (sun, moon, stars).
 */
@Mixin(WorldRenderer.class)
public class MixinSkyElements {

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlSkyRendering(CallbackInfo ci) {
        // Disable entire sky when option is off
        // TODO: Find more specific injection points to target only sky gradient
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.detailSettings.sky) {
            ci.cancel();
            return;
        }
    }
    
    // Future improvement ideas:
    // 1. Target specific GL calls or buffer operations for sky gradient only
    // 2. Use @ModifyArg to change sky colors to transparent instead of canceling
    // 3. Find injection points after sky gradient but before celestial object rendering
    // 4. Use @Slice to target specific portions of the renderSky method
}