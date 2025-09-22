package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls sky element rendering with multi-version support
 * Uses optional injections for maximum compatibility across Minecraft versions
 *
 * CURRENT LIMITATION: This mixin currently disables the entire sky (gradient, sun, moon, stars)
 * when the sky option is turned off. This is a temporary approach until more granular
 * injection points can be identified for targeting only the sky gradient.
 */
@Mixin(WorldRenderer.class)
public class MixinSkyElements {

    /**
     * Primary sky rendering injection
     * Uses generic method targeting for maximum compatibility
     */
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlSkyRendering(CallbackInfo ci) {
        // Disable entire sky when option is off
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.detailSettings.sky) {
            ci.cancel();
            return;
        }
    }

    /**
     * Alternative sky rendering injection for different method signatures
     * Targets any renderSky method regardless of parameters
     */
    @Inject(method = "renderSky*", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlSkyRenderingWildcard(CallbackInfo ci) {
        // Disable entire sky when option is off
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.detailSettings.sky) {
            ci.cancel();
            return;
        }
    }
}