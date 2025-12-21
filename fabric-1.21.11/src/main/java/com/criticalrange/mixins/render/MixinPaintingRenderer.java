package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.render.entity.state.PaintingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Painting rendering control mixin for Minecraft 1.21.9+
 * 
 * MAJOR CHANGE in 1.21.9:
 * - EntityRenderer now uses RenderState objects with OrderedRenderCommandQueue
 * - render() signature: (PaintingEntityRenderState, MatrixStack, OrderedRenderCommandQueue, CameraRenderState)V
 * - VertexConsumerProvider replaced with OrderedRenderCommandQueue
 * 
 * Uses require=0 to fail gracefully if signature doesn't match.
 */
@Mixin(PaintingEntityRenderer.class)
public class MixinPaintingRenderer {

    /**
     * 1.21.9+: Render state-based render method with OrderedRenderCommandQueue
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/PaintingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlPaintingRendering(PaintingEntityRenderState state, MatrixStack matrices, 
            OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null && 
            !VulkanModExtra.CONFIG.renderSettings.painting) {
            ci.cancel();
        }
    }
}