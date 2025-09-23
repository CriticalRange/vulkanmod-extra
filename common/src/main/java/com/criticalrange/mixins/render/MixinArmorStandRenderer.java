package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * UNIVERSAL PATTERN: Multi-version entity rendering control
 *
 * Copy-paste template for any entity renderer:
 * 1. Replace "ArmorStand" with your entity type
 * 2. Replace "armorStand" with your config setting
 * 3. Replace ArmorStandEntityRenderer with your specific renderer
 * 4. Replace ArmorStandEntity with your entity class
 *
 * NOTE: ArmorStands use LivingEntityRenderer for 1.21.1 (they don't have direct render method)
 */
@Mixin(LivingEntityRenderer.class)
public class MixinArmorStandRenderer {

    /**
     * 1.21.1: Entity-based render method with instanceof filtering
     * This method signature only exists in 1.21.1
     */
    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void controlRendering1_21_1(LivingEntity entity, float yaw, float tickDelta,
                                       MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider,
                                       int light, CallbackInfo ci) {
        if (entity instanceof ArmorStandEntity) {
            if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.armorStand) {
                ci.cancel();
            }
        }
    }
}