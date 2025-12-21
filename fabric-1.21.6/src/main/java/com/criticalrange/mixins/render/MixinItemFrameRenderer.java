package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Item frame rendering control mixin for Minecraft 1.21.2+
 * Uses new render state architecture with correct method signatures
 */
@Mixin(ItemFrameEntityRenderer.class)
public class MixinItemFrameRenderer {

    /**
     * 1.21.2+: Render state-based render method
     * New architecture: render(RenderState, MatrixStack, VertexConsumerProvider, int)
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlItemFrameRendering1_21_2(ItemFrameEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            if (!VulkanModExtra.CONFIG.renderSettings.itemFrame) {
                ci.cancel();
            }
        }
    }

    /**
     * Universal: hasLabel method (works across all versions)
     * For 1.21.1: hasLabel(ItemFrameEntity)
     * For 1.21.2+: hasLabel() with no parameters
     */
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlNameTag(CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            if (!VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag) {
                cir.setReturnValue(false);
            }
        }
    }
}