package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.util.VersionHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Performance-focused fog optimization mixin
 * Optimizes fog calculations for better performance
 * Uses MixinExtras for version-conditional method handling
 */
@Mixin(BackgroundRenderer.class)
public class MixinFogRenderer {

    // Simplified fog optimization - tracks setup calls
    private static int fogUpdateCounter = 0;

    /**
     * Version-conditional fog application
     * 1.21.1: applyFog() with CallbackInfo
     * 1.21.2+: applyFog() with CallbackInfoReturnable<Fog>
     */
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void vulkanmodExtra$trackFogUpdates(CallbackInfo ci) {
        fogUpdateCounter++;

        // Check global fog setting
        if (!VulkanModExtra.CONFIG.renderSettings.globalFog) {
            // If global fog is disabled, cancel fog application
            ci.cancel();
            return;
        }

        // Reset counter to prevent overflow
        if (fogUpdateCounter > 10000) {
            fogUpdateCounter = 0;
        }
    }

    /**
     * Version-conditional fog application for 1.21.2+ with return value
     * Note: This method will only be active in 1.21.2+ where Fog class exists
     */
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void vulkanmodExtra$trackFogUpdatesWithReturn(CallbackInfoReturnable<Object> cir) {
        if (!VersionHelper.IS_POST_1_21_1) {
            return; // Only apply this in 1.21.2+
        }

        fogUpdateCounter++;

        // Check global fog setting
        if (!VulkanModExtra.CONFIG.renderSettings.globalFog) {
            // If global fog is disabled, return null fog
            cir.setReturnValue(null); // Return null fog for 1.21.2+
            return;
        }

        // Reset counter to prevent overflow
        if (fogUpdateCounter > 10000) {
            fogUpdateCounter = 0;
        }
    }

    /**
     * Wrap fog operations to handle version-specific return types
     */
    @WrapOperation(method = "applyFog", at = @At(value = "INVOKE", target = "applyFog", remap = false))
    private static Object vulkanmodExtra$wrapApplyFog(BackgroundRenderer renderer, Operation<Object> original,
                                                       Object... args) {
        // Always check our config first
        if (!VulkanModExtra.CONFIG.renderSettings.globalFog) {
            if (VersionHelper.IS_POST_1_21_1) {
                // Return null fog for 1.21.2+
                return null;
            } else {
                // Return void for 1.21.1
                return null;
            }
        }

        // Handle version-specific fog application
        if (VersionHelper.IS_POST_1_21_1) {
            // 1.21.2+: Return Fog object
            try {
                Object result = original.call(renderer, args);
                // Check if result is a Fog object using reflection
                if (result != null && result.getClass().getSimpleName().equals("Fog")) {
                    return result;
                } else {
                    return null;
                }
            } catch (Exception e) {
                VulkanModExtra.LOGGER.warn("Failed to apply fog for 1.21.2+", e);
                return null;
            }
        } else {
            // 1.21.1: Void return
            try {
                original.call(renderer, args);
                return null;
            } catch (Exception e) {
                VulkanModExtra.LOGGER.warn("Failed to apply fog for 1.21.1", e);
                return null;
            }
        }
    }

    /**
     * Get current fog update count for debugging
     */
    public static int getFogUpdateCount() {
        return fogUpdateCounter;
    }

    /**
     * Reset fog update counter
     */
    public static void resetFogUpdateCount() {
        fogUpdateCounter = 0;
    }
}
