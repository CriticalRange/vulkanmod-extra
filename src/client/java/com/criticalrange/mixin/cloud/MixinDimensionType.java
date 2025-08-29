package com.criticalrange.mixin.cloud;

import com.criticalrange.VulkanModExtra;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Performance-focused cloud optimization mixin
 * Optimizes cloud rendering for better performance based on configuration
 */
@Mixin(DimensionType.class)
public class MixinDimensionType {

    @Inject(method = "hasSkyLight", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$optimizeCloudRendering(CallbackInfoReturnable<Boolean> cir) {
        // Disable sky light (and thus clouds) in certain performance scenarios
        if (VulkanModExtra.CONFIG.environmentSettings.cloudDistance <= 32) {
            // Very low cloud distance - disable sky light processing for better performance
            cir.setReturnValue(false);
            return;
        }
    }

    // Note: cloudHeight method might not be available in this Minecraft version
    // @Inject(method = "cloudHeight()F", at = @At("HEAD"), cancellable = true)
    // private void vulkanmodExtra$customCloudHeight(CallbackInfoReturnable<Float> cir) {
    //     // Allow custom cloud height configuration for performance and visual preferences
    //     if (VulkanModExtra.CONFIG.extraSettings.cloudHeight != 192) { // 192 is default
    //         float customHeight = (float) VulkanModExtra.CONFIG.extraSettings.cloudHeight;
    //         cir.setReturnValue(customHeight);
    //     }
    // }
}
