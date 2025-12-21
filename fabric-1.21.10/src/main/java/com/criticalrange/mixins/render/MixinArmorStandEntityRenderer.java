package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Armor stand rendering control mixin for Minecraft 1.21.9+
 * 
 * MAJOR CHANGE in 1.21.9:
 * - EntityRenderer now uses RenderState objects with OrderedRenderCommandQueue
 * - render() signature: (ArmorStandEntityRenderState, MatrixStack, OrderedRenderCommandQueue, CameraRenderState)V
 * - VertexConsumerProvider replaced with OrderedRenderCommandQueue
 * 
 * Uses require=0 to fail gracefully if signature doesn't match.
 */
@Mixin(ArmorStandEntityRenderer.class)
public class MixinArmorStandEntityRenderer {

    /**
     * 1.21.9+: Render state-based render method with OrderedRenderCommandQueue
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ArmorStandEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlRendering(ArmorStandEntityRenderState state, MatrixStack matrices, 
            OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null && 
            !VulkanModExtra.CONFIG.renderSettings.armorStand) {
            ci.cancel();
        }
    }

    /**
     * hasLabel method - signature may have changed in 1.21.9
     * Using require=0 to fail gracefully
     */
    @Inject(method = "hasLabel(Lnet/minecraft/entity/decoration/ArmorStandEntity;D)Z", 
            at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlArmorStandLabel(ArmorStandEntity entity, double distance, 
            CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null && 
            !VulkanModExtra.CONFIG.renderSettings.armorStandNameTag) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Alternative hasLabel for 1.21.9+ if signature changed to use RenderState
     */
    @Inject(method = "hasLabel(Lnet/minecraft/client/render/entity/state/ArmorStandEntityRenderState;)Z", 
            at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlArmorStandLabelRenderState(ArmorStandEntityRenderState state, 
            CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null && 
            !VulkanModExtra.CONFIG.renderSettings.armorStandNameTag) {
            cir.setReturnValue(false);
        }
    }
}