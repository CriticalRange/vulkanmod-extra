package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ArmorStand rendering control via LivingEntityRenderer
 * ArmorStandEntityRenderer extends LivingEntityRenderer, so we intercept there
 */
@Mixin(net.minecraft.client.render.entity.LivingEntityRenderer.class)
public class MixinArmorStandRenderer {

    /**
     * 1.21.1: LivingEntityRenderer.render method with ArmorStand filtering
     */
    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void controlRendering1_21_1(net.minecraft.entity.LivingEntity livingEntity, float f, float g,
                                       net.minecraft.client.util.math.MatrixStack matrixStack, net.minecraft.client.render.VertexConsumerProvider vertexConsumerProvider,
                                       int i, CallbackInfo ci) {
        if (livingEntity instanceof ArmorStandEntity) {
            if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null && !VulkanModExtra.CONFIG.renderSettings.armorStand) {
                ci.cancel();
            }
        }
    }
}