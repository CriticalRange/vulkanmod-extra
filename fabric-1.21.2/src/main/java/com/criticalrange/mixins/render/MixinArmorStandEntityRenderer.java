package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * UNIVERSAL PATTERN: 1.21.2+ ArmorStand rendering control
 *
 * In 1.21.2+, ArmorStandEntityRenderer overrides its own render method with render state architecture
 * In 1.21.1, ArmorStands are handled by MixinArmorStandRenderer (LivingEntityRenderer)
 */
@Mixin(ArmorStandEntityRenderer.class)
public class MixinArmorStandEntityRenderer {

    /**
     * 1.21.2: Render state-based render method
     * Parameters: ArmorStandEntityRenderState, MatrixStack, VertexConsumerProvider, int
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ArmorStandEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void controlRendering1_21_2(ArmorStandEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null && !VulkanModExtra.CONFIG.renderSettings.armorStand) {
            ci.cancel();
        }
    }

    /**
     * 1.21.2: hasLabel method with ArmorStandEntity parameter and distance
     */
    @Inject(method = "hasLabel(Lnet/minecraft/entity/decoration/ArmorStandEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void controlNameTag(net.minecraft.entity.decoration.ArmorStandEntity armorStandEntity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null && !VulkanModExtra.CONFIG.renderSettings.armorStandNameTag) {
            cir.setReturnValue(false);
        }
    }
}