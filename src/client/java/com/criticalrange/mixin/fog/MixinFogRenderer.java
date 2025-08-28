package com.criticalrange.mixin.fog;

import com.criticalrange.client.config.VulkanModExtraClientConfig;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to control fog rendering based on VulkanMod Extra settings
 * Based on sodium-extra's fog control system
 */
@Mixin(FogRenderer.class)
public class MixinFogRenderer {
    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void setupFog(CallbackInfo ci) {
        if (!VulkanModExtraClientConfig.getInstance().renderSettings.globalFog) {
            ci.cancel();
        }
    }
}
