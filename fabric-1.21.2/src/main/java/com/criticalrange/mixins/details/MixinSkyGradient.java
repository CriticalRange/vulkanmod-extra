package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sky gradient control mixin for Minecraft 1.21.2
 * Controls sky background gradient rendering by targeting cloud buffer building
 */
@Mixin(WorldRenderer.class)
public class MixinSkyGradient {

    /**
     * Control sky gradient rendering in Minecraft 1.21.2
     * Sky gradient is part of the cloud buffer building process
     */
    @Inject(method = "buildClouds", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlSkyGradient(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.detailSettings != null &&
            !VulkanModExtra.CONFIG.detailSettings.skyGradient) {
            ci.cancel();
        }
    }
}