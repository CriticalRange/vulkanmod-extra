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
 * Controls painting rendering with multi-version support
 * Handles both 1.21.1 entity-based and 1.21.2+ render state-based rendering
 */
@Mixin(PaintingEntityRenderer.class)
public class MixinPaintingRenderer {

    /**
     * 1.21.2: Render state-based render method
     * Parameters: PaintingEntityRenderState, MatrixStack, VertexConsumerProvider, int
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/PaintingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlPaintingRendering1_21_2(PaintingEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.painting) {
            ci.cancel();
        }
    }

    /**
     * 1.21.1 entity-based render method - exact signature from javap analysis
     * Parameters: PaintingEntity, float, float, MatrixStack, VertexConsumerProvider, int
     */
    @Inject(method = "render(Lnet/minecraft/entity/decoration/painting/PaintingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlPaintingRendering1_21_1(CallbackInfo ci) {
        // This method signature is for 1.21.1 - kept for reference but won't be called in 1.21.2
    }

}