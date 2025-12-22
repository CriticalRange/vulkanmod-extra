package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.21.1 ItemFrame rendering control
 *
 * Uses entity-based rendering architecture
 */
@Mixin(ItemFrameEntityRenderer.class)
public class MixinItemFrameRenderer {

    /**
     * 1.21.1: Entity-based render method
     * Method signature: render(T, float, float, MatrixStack, VertexConsumerProvider, int)
     */
    @Inject(method = "render(Lnet/minecraft/entity/decoration/ItemFrameEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void controlRendering1_21_1(ItemFrameEntity itemFrameEntity, float f, float g, net.minecraft.client.util.math.MatrixStack matrixStack, net.minecraft.client.render.VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.renderSettings != null &&
            !VulkanModExtra.CONFIG.renderSettings.itemFrame) {
            ci.cancel();
        }
    }

    /**
     * 1.21.1: hasLabel method with ItemFrameEntity parameter
     */
    @Inject(method = "hasLabel(Lnet/minecraft/entity/decoration/ItemFrameEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void controlNameTag(ItemFrameEntity itemFrameEntity, CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.renderSettings != null &&
            !VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag) {
            cir.setReturnValue(false);
        }
    }
}