package com.criticalrange.mixin.entity_rendering;

import com.criticalrange.client.config.VulkanModExtraClientConfig;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.world.entity.decoration.Painting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls painting rendering based on configuration
 */
@Mixin(PaintingRenderer.class)
public class MixinPaintingRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo ci) {
        if (!VulkanModExtraClientConfig.getInstance().renderSettings.painting) {
            ci.cancel();
        }
    }
}