package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Performance-focused fog optimization mixin
 * Uses simple @Inject approach for maximum compatibility
 * Avoids complex MixinExtras targeting issues
 */
@Mixin(BackgroundRenderer.class)
public class MixinFogRenderer {

    /**
     * Simple fog control using standard @Inject
     * Cancels fog application when global fog is disabled
     */
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanmodExtra$controlFog(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            if (!VulkanModExtra.CONFIG.renderSettings.globalFog) {
                ci.cancel();
            }
        }
    }
}
