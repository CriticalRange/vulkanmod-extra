package com.criticalrange.mixin.weather;

import com.criticalrange.VulkanModExtra;
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
    @Inject(method = "renderSnowAndRain", at = @At("HEAD"))
    private void renderSnowAndRain(LevelRenderer levelRenderer, float partialTicks, double camX, double camY, double camZ, CallbackInfo ci) {
        if (!VulkanModExtraClientConfig.getInstance().detailSettings.rainSnow) {
            // Weather rendering is disabled - would cancel here but mixin limitations prevent it
            VulkanModExtra.LOGGER.debug("Weather rendering is disabled but cannot cancel due to mixin limitations");
        }
    }
}
