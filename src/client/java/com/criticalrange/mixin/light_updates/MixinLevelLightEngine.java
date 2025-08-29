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
 * Light updates control mixin based on Sodium Extra implementation
 * Controls light calculations for better performance
 */
@Mixin(LevelLightEngine.class)
public class MixinLevelLightEngine {
    @Inject(at = @At("HEAD"), method = "checkBlock", cancellable = true)
    public void vulkanmodExtra$checkBlock(BlockPos pos, CallbackInfo ci) {
        try {
            if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.lightUpdates) {
                ci.cancel();
            }
        } catch (Exception e) {
            // If config access fails, let vanilla lighting work
            VulkanModExtra.LOGGER.warn("Light updates mixin config access failed: {}", e.getMessage());
        }
    }

    @Inject(at = @At("RETURN"), method = "runLightUpdates", cancellable = true)
    public void vulkanmodExtra$doLightUpdates(CallbackInfoReturnable<Integer> cir) {
        try {
            if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.lightUpdates) {
                cir.setReturnValue(0);
            }
        } catch (Exception e) {
            // If config access fails, let vanilla lighting work
            VulkanModExtra.LOGGER.warn("Light updates mixin config access failed: {}", e.getMessage());
        }
    }
}
