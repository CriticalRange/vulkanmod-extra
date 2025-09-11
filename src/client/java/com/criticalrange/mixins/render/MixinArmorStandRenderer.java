package com.criticalrange.mixins.render;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls armor stand rendering based on configuration
 * Mixes into LivingEntityRenderer to target ArmorStandEntity specifically
 */
@Mixin(LivingEntityRenderer.class)
public class MixinArmorStandRenderer {

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", cancellable = true)
    public void vulkanmodExtra$onRenderArmorStand(net.minecraft.entity.LivingEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (entity instanceof ArmorStandEntity) {
            try {
                var configManager = com.criticalrange.config.ConfigurationManager.getInstance();
                var config = configManager.getConfig();
                if (!config.renderSettings.armorStand) {
                    ci.cancel();
                }
            } catch (Exception e) {
                // If config fails to load, default to rendering armor stands
            }
        }
    }
}