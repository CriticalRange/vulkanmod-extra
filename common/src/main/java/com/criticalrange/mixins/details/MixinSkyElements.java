package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls complete sky rendering when master toggle is disabled
 * Uses optional injections for maximum compatibility across Minecraft versions
 *
 * Note: Individual sky elements (gradient, sun, moon, stars) are controlled by MixinWorldRendererCelestial
 */
@Mixin(WorldRenderer.class)
public class MixinSkyElements {

    /**
     * Master sky rendering control - cancels entire renderSky when master toggle is off
     */
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlMasterSkyToggle(CallbackInfo ci) {
        // Only cancel when master toggle is completely off
        if (VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.detailSettings != null &&
            !VulkanModExtra.CONFIG.detailSettings.sky) {
            ci.cancel();
            return;
        }
    }

    /**
     * Alternative master sky control for different method signatures
     */
    @Inject(method = "renderSky*", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlMasterSkyToggleWildcard(CallbackInfo ci) {
        // Only cancel when master toggle is completely off
        if (VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.detailSettings != null &&
            !VulkanModExtra.CONFIG.detailSettings.sky) {
            ci.cancel();
            return;
        }
    }
}