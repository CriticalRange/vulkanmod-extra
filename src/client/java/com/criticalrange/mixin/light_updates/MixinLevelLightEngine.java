package com.criticalrange.mixin.light_updates;

import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Performance-focused light updates optimization mixin
 * Optimizes light calculations for better performance
 */
@Mixin(LevelLightEngine.class)
public class MixinLevelLightEngine {

    // Counter to throttle light updates
    private static int lightUpdateCounter = 0;

    @Inject(method = "runLightUpdates", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$optimizeLightUpdates(CallbackInfoReturnable<Integer> cir) {
        lightUpdateCounter++;

        // Throttle light updates for performance - only run every 2nd update
        // This can significantly improve performance in areas with frequent light changes
        if (lightUpdateCounter % 2 != 0) {
            cir.setReturnValue(0); // Skip this light update
            return;
        }

        // Reset counter to prevent overflow
        if (lightUpdateCounter > 10000) {
            lightUpdateCounter = 0;
        }
    }
}
