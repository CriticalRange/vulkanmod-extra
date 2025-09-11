package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls piston rendering based on configuration
 * 1.21.4 version with entity parameter
 */
@Mixin(PistonBlockEntityRenderer.class)
public class MixinPistonRenderer {

    @Inject(method = "render(Lnet/minecraft/block/entity/PistonBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At("HEAD"), cancellable = true)
    private void onRender(PistonBlockEntity pistonBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.piston) {
            ci.cancel();
        }
    }
}