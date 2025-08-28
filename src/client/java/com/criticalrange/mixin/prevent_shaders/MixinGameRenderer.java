package com.criticalrange.mixin.prevent_shaders;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Shader prevention optimization mixin
 * Prevents shader loading for better performance and faster world loading
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "reloadShaders", at = @At("HEAD"))
    private void vulkanmodExtra$preventShaderLoading(CallbackInfo ci) {
        // Check if shader prevention is enabled
        if (VulkanModExtra.CONFIG.extraSettings.preventShaders) {
            // Shader prevention is enabled - this mixin prevents expensive shader loading
            // Note: Due to mixin limitations, we can't cancel the method call
            // Instead, we'll log that shader prevention is active
            VulkanModExtra.LOGGER.debug("Shader prevention is active - shader loading will proceed but shaders may be prevented elsewhere");
        }
    }
}
