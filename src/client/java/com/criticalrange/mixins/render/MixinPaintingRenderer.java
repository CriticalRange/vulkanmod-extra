package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls painting rendering with multi-version support
 * Handles both 1.21.1 entity-based and 1.21.2+ render state-based rendering
 */
@Mixin(PaintingEntityRenderer.class)
public class MixinPaintingRenderer {

    /**
     * Universal painting rendering injection - targets any render method
     * Works across all Minecraft versions by targeting method name only
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlPaintingRendering(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.painting) {
            ci.cancel();
        }
    }
}