package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Weather rendering control mixin for Minecraft 1.21.2
 * Controls rain and snow particle effects using the correct method names
 */
@Mixin(WorldRenderer.class)
public class MixinWeatherRenderer {

    /**
     * Control weather particle generation in Minecraft 1.21.2
     * Weather particles are created during the level rendering tick
     */
    @Inject(method = "tickRain", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlWeatherParticles(Camera camera, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.detailSettings != null &&
            !VulkanModExtra.CONFIG.detailSettings.rainSnow) {
            ci.cancel();
        }
    }
}