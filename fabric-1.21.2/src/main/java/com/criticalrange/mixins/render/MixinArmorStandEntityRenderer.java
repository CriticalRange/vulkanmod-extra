package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.21.2+ Enhanced armor stand rendering control
 * Supports both entity-based and render state-based architectures
 */
@Mixin(ArmorStandEntityRenderer.class)
public class MixinArmorStandEntityRenderer {

    /**
     * 1.21.2+: Render state-based method
     * Method signature: render(ArmorStandEntityRenderState, MatrixStack, VertexConsumerProvider, int)
     * This method only exists in 1.21.2+
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ArmorStandEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void controlRendering1_21_2Plus(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.armorStand) {
            ci.cancel();
        }
    }

    /**
     * Name tag control for armor stands
     */
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void controlNameTag(CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.armorStandNameTag) {
            cir.setReturnValue(false);
        }
    }
}