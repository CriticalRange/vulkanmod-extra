package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Celestial bodies rendering control mixin for Minecraft 1.21.2
 * Controls sun, moon, and stars rendering in the renderSky method
 */
@Mixin(WorldRenderer.class)
public class MixinCelestialRendering {

    /**
     * Control celestial bodies rendering in Minecraft 1.21.2
     * Uses the renderSky method which handles sun, moon, and stars
     */
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlCelestialBodies(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.detailSettings != null) {
            var settings = VulkanModExtra.CONFIG.detailSettings;

            // Cancel celestial bodies when all are disabled for performance
            // Individual control is handled by platform-specific mixins
            if (!settings.sun && !settings.moon && !settings.stars) {
                ci.cancel();
            }
        }
    }
}