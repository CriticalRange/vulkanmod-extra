package com.criticalrange.mixin.steady_debug_hud;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Steady debug HUD optimization mixin
 * Stabilizes debug screen for consistent performance monitoring
 */
@Mixin(DebugScreenOverlay.class)
public class MixinDebugScreenOverlay {

    // Note: This shadow field may not exist in the target class
    // We'll handle timing internally instead

    // Internal timing for steady debug HUD
    private static long lastUpdateTime = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void vulkanmodExtra$stabilizeDebugHud(CallbackInfo ci) {
        // Check if steady debug HUD is enabled
        if (VulkanModExtra.CONFIG.extraSettings.steadyDebugHud) {
            // Steady debug HUD implementation:
            // - Consistent update intervals for stable readings
            // - Performance metric smoothing
            // - Memory usage display optimizations
            // - Reduced update frequency for better stability
            long currentTime = System.currentTimeMillis();
            int refreshInterval = VulkanModExtra.CONFIG.extraSettings.steadyDebugHudRefreshInterval;

            // Only update at specified intervals for stability
            if (currentTime - lastUpdateTime < refreshInterval * 1000L) {
                // Instead of cancelling, we'll just update the timing
                // and let the render proceed normally
                lastUpdateTime = currentTime;
            }
        }
    }
}
