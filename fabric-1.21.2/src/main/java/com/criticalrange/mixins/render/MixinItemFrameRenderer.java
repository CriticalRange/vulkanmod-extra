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
 * UNIVERSAL PATTERN: Multi-version entity rendering control
 *
 * Copy-paste template for any entity renderer:
 * 1. Replace "ItemFrame" with your entity type
 * 2. Replace "itemFrame" with your config setting
 * 3. Replace ItemFrameEntityRenderer with your specific renderer
 * 4. Replace ItemFrameEntity with your entity class
 */
@Mixin(ItemFrameEntityRenderer.class)
public class MixinItemFrameRenderer {

    /**
     * 1.21.2: Render state-based render method
     * Parameters: ItemFrameEntityRenderState, MatrixStack, VertexConsumerProvider, int
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void controlRendering1_21_2(ItemFrameEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.itemFrame) {
            ci.cancel();
        }
    }

    /**
     * 1.21.1: Entity-based render method (kept for reference)
     */
    @Inject(method = "render(Lnet/minecraft/entity/decoration/ItemFrameEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void controlRendering1_21_1(CallbackInfo ci) {
        // This method signature is for 1.21.1 - kept for reference but won't be called in 1.21.2
    }

    /**
     * Universal: hasLabel method (works across all versions)
     * For 1.21.1: hasLabel(ItemFrameEntity, double)
     * For 1.21.2+: hasLabel(T, double) where T extends ItemFrameEntity
     */
    @Inject(method = "hasLabel(Lnet/minecraft/entity/decoration/ItemFrameEntity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void controlNameTag(net.minecraft.entity.decoration.ItemFrameEntity itemFrame, double double_1, CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Universal: hasLabel method for Entity parent
     */
    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void controlNameTagEntity(net.minecraft.entity.Entity entity, double double_1, CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag && entity instanceof net.minecraft.entity.decoration.ItemFrameEntity) {
            cir.setReturnValue(false);
        }
    }
}