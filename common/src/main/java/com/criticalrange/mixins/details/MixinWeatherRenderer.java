package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Weather rendering control mixin
 * Controls rain and snow particle effects and weather sounds
 * Platform-specific versions will override this with correct method names
 */
@Mixin(WorldRenderer.class)
public class MixinWeatherRenderer {

    /**
     * Control weather particles and sound effects
     * This method will be overridden by platform-specific mixins
     */
    @Inject(method = "*", at = @At("HEAD"), require = 0)
    private void vulkanmodExtra$controlWeatherParticles(Camera camera, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.detailSettings != null) {
            // Cancel weather effects when rain/snow toggle is disabled
            if (!VulkanModExtra.CONFIG.detailSettings.rainSnow) {
                ci.cancel();
            }
        }
    }
}