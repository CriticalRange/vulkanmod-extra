package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.render.entity.state.PaintingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Painting rendering control mixin for Minecraft 1.21.2+
 * Uses new render state architecture with correct method signatures
 */
@Mixin(PaintingEntityRenderer.class)
public class MixinPaintingRenderer {

    /**
     * 1.21.2+: Render state-based render method
     * New architecture: render(PaintingEntityRenderState, MatrixStack, VertexConsumerProvider, int)
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/PaintingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlPaintingRendering1_21_2(PaintingEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            if (!VulkanModExtra.CONFIG.renderSettings.painting) {
                ci.cancel();
            }
        }
    }

}