package com.criticalrange.mixins.details;

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
 * Simplified to use only @Inject for compatibility
 */
@Mixin(WorldRenderer.class)
public class MixinLevelRenderer {

    /**
     * Weather rendering injection for Minecraft 1.21.1
     * Method signature: renderWeather(LightmapTextureManager, float, double, double, double)
     */
    @Inject(method = "renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$renderSnowAndRain(LightmapTextureManager manager, float tickDelta,
                                                   double cameraX, double cameraY, double cameraZ,
                                                   CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.detailSettings.rainSnow) {
            ci.cancel();
        }
    }
}