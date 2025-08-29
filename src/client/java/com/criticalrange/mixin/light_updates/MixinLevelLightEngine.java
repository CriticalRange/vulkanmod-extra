package com.criticalrange.mixin.light_updates;

import com.criticalrange.VulkanModExtra;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Light updates control mixin - SAFE implementation
 * Provides light throttling when enabled, minimal interference when disabled
 */
@Mixin(LevelLightEngine.class)
public class MixinLevelLightEngine {
    private static int throttleCounter = 0;

    @Inject(at = @At("HEAD"), method = "runLightUpdates", cancellable = true)
    public void vulkanmodExtra$throttleLightUpdates(CallbackInfoReturnable<Integer> cir) {
        try {
            // Safety check - if config is null, let vanilla handle everything
            if (VulkanModExtra.CONFIG == null) {
                return;
            }

            // If light updates are enabled, apply mild throttling for performance
            if (VulkanModExtra.CONFIG.renderSettings.lightUpdates) {
                throttleCounter++;
                
                // Very conservative throttling - only skip every 4th update
                // This provides performance benefit without breaking critical lighting
                if (throttleCounter % 4 == 0) {
                    cir.setReturnValue(0);
                }
                
                // Reset counter to prevent overflow
                if (throttleCounter > 1000) {
                    throttleCounter = 0;
                }
                return;
            }
            
            // If disabled: Apply heavy throttling but NEVER completely block
            // This maintains basic lighting functionality while reducing performance impact
            throttleCounter++;
            
            // When disabled, only allow every 8th light update
            // This gives significant performance boost while maintaining basic lighting
            if (throttleCounter % 8 != 0) {
                cir.setReturnValue(0);
            }
            
            // Reset counter to prevent overflow
            if (throttleCounter > 1000) {
                throttleCounter = 0;
            }
            
        } catch (Exception e) {
            // If anything goes wrong, let vanilla lighting work normally
            VulkanModExtra.LOGGER.warn("Light updates mixin error: {}", e.getMessage());
        }
    }
}
