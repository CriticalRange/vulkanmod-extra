package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Weather effect control mixin with multi-version support
 * Controls weather rendering (rain and snow) for better performance
 * Uses optional injections for maximum compatibility
 */
@Mixin(WorldRenderer.class)
public class MixinLevelRenderer {

    /**
     * Primary weather rendering injection
     * Attempts to target renderWeather with full signature (works on 1.21.1)
     */
    @Inject(method = "renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$renderSnowAndRain(LightmapTextureManager manager, float tickDelta,
                                                   double cameraX, double cameraY, double cameraZ,
                                                   CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.detailSettings.rainSnow) {
            ci.cancel();
        }
    }

    /**
     * Alternative weather rendering injection for newer versions
     * Uses broader method targeting without specific parameters
     */
    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$renderWeatherGeneric(CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.detailSettings.rainSnow) {
            ci.cancel();
        }
    }
}