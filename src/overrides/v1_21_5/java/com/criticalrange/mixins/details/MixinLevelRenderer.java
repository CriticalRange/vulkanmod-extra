package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Weather effect control mixin based on Sodium Extra pattern
 * Controls weather rendering (rain and snow) for better performance
 * Updated for Minecraft 1.21.5+ with LightmapTextureManager parameter removed
 */
@Mixin(WorldRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderWeather", at = @At(value = "HEAD"), cancellable = true)
    private void vulkanmodExtra$renderSnowAndRain(FrameGraphBuilder frameGraphBuilder, Vec3d cameraPos, float tickProgress, Fog fog, CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.detailSettings.rainSnow) {
            ci.cancel();
        }
    }
}