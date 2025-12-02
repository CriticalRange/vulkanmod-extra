package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.CloudRenderer;
import net.minecraft.client.option.CloudRenderMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cloud rendering control mixin for Minecraft 1.21.2
 * Controls cloud rendering based on user preferences and performance settings
 * Uses the new CloudRenderer system introduced in 1.21.2
 */
@Mixin(CloudRenderer.class)
public class MixinSkyElements {

    /**
     * Cloud rendering control - modifies cloud behavior based on settings
     * This replaces the problematic sky master toggle that was removing the entire sky
     */
    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlCloudRendering(int tickDelta, CloudRenderMode cloudRenderMode,
                                                     float f, org.joml.Matrix4f matrix4f, org.joml.Matrix4f matrix4f2,
                                                     net.minecraft.util.math.Vec3d vec3d, float g, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.detailSettings != null) {
            var settings = VulkanModExtra.CONFIG.detailSettings;

            // Disable clouds completely if distance is set to 0 or below
            if (settings.cloudDistance <= 0) {
                ci.cancel();
                return;
            }

            // Very low cloud distance - disable clouds for performance
            if (settings.cloudDistance <= 32) {
                ci.cancel();
                return;
            }
        }
    }
}