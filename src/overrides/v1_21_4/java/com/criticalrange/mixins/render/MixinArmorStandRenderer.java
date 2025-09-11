package com.criticalrange.mixins.render;

import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls armor stand rendering based on configuration
 * 1.21.4+ version with ArmorStandEntityRenderState parameter
 */
@Mixin(ArmorStandEntityRenderer.class)
public class MixinArmorStandRenderer {

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/render/entity/state/ArmorStandEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", cancellable = true)
    public void vulkanmodExtra$onRender(ArmorStandEntityRenderState state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
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