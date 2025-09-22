package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls armor stand rendering by targeting the render method with exact parameters
 * ArmorStandEntityRenderer inherits render from LivingEntityRenderer
 */
@Mixin(ArmorStandEntityRenderer.class)
public class MixinArmorStandRenderer {

    /**
     * Targets the setupTransforms method which is called during armor stand rendering
     * This method is actually overridden in ArmorStandEntityRenderer
     */
    @Inject(method = "setupTransforms", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlArmorStandRendering(ArmorStandEntity armorStandEntity, MatrixStack matrixStack,
                                                         float f, float g, float h, float i, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.armorStand) {
            ci.cancel();
        }
    }
}