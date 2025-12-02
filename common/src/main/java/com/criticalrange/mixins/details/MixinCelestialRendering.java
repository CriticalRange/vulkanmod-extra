package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Celestial bodies rendering control mixin
 * Controls sun, moon, and stars rendering using wildcard patterns
 * Platform-specific versions will override this with correct method names
 */
@Mixin(WorldRenderer.class)
public class MixinCelestialRendering {

    /**
     * Control celestial bodies rendering using wildcard patterns
     * This method will be overridden by platform-specific mixins
     */
    @Inject(method = "*", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlCelestialBodies(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.detailSettings != null) {
            var settings = VulkanModExtra.CONFIG.detailSettings;

            // Cancel celestial bodies when all are disabled for performance
            // Platform-specific versions will provide more precise control
            if (!settings.sun && !settings.moon && !settings.stars) {
                ci.cancel();
            }
        }
    }
}