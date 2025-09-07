package com.criticalrange.mixin.weather;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Weather effect control mixin based on Sodium Extra pattern
 * Controls weather rendering (rain and snow) for better performance
 */
@Mixin(WorldRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderWeather", at = @At(value = "HEAD"), cancellable = true)
    private void vulkanmodExtra$renderSnowAndRain(LightmapTextureManager lightTexture, float f, double d, double e, double g, CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.detailSettings.rainSnow) {
            ci.cancel();
        }
    }
}