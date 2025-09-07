package com.criticalrange.mixin.prevent_shaders;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Shader prevention optimization mixin
 * Prevents shader loading for better performance and faster world loading
 * Implementation based on proven Sodium Extra pattern
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "togglePostProcessorEnabled", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$preventShaderToggle(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG.extraSettings.preventShaders) {
            ci.cancel();
        }
    }

    // Note: setPostEffect method may not exist in this Minecraft version
    // @Inject(method = "setPostEffect", at = @At("HEAD"), cancellable = true)
    // private void vulkanmodExtra$preventShaderSet(Identifier identifier, CallbackInfo ci) {
    //     if (VulkanModExtra.CONFIG.extraSettings.preventShaders) {
    //         ci.cancel();
    //     }
    // }
}
