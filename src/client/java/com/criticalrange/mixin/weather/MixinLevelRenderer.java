package com.criticalrange.mixin.weather;

import com.criticalrange.client.config.VulkanModExtraClientConfig;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to control weather rendering (rain and snow) based on VulkanMod Extra settings
 * Based on sodium-extra's weather control system
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    private void renderSnowAndRain(LevelRenderer levelRenderer, float partialTicks, double camX, double camY, double camZ, CallbackInfo ci) {
        if (!VulkanModExtraClientConfig.getInstance().detailSettings.rainSnow) {
            ci.cancel();
        }
    }
}
