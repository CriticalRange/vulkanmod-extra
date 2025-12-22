package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Efficient beacon beam height control mixin
 * Directly modifies the height value instead of canceling entire rendering
 * Based on Minecraft 1.21.1 source code analysis
 */
@Mixin(BeaconBlockEntityRenderer.class)
public class MixinBeaconRenderer {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$checkBeaconRendering(BeaconBlockEntity beaconBlockEntity, float partialTick,
            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
            int packedLight, int packedOverlay, CallbackInfo ci) {
        // Cancel beacon beam rendering if disabled
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null &&
            !VulkanModExtra.CONFIG.renderSettings.beaconBeam) {
            ci.cancel();
        }
    }

    /**
     * Efficiently modify beacon beam height by intercepting the hardcoded 1024 value
     * This targets: renderBeam(..., m == list.size() - 1 ? 1024 : beamSegment.getHeight(), ...)
     * Much more efficient than canceling the entire render
     */
    @ModifyArg(method = "render", at = @At(value = "INVOKE",
               target = "Lnet/minecraft/client/render/block/entity/BeaconBlockEntityRenderer;renderBeam(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;FJIII)V"),
               index = 5) // maxY parameter (6th parameter, 0-indexed = 5)
    private int vulkanmodExtra$modifyBeaconHeight(int originalHeight) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            // If this is the hardcoded 1024 (top segment), replace with our configured height
            if (originalHeight == 1024) {
                int configuredHeight = VulkanModExtra.CONFIG.renderSettings.beaconBeamHeight;
                // Clamp to valid range (32-512)
                return Math.max(32, Math.min(512, configuredHeight));
            }
        }
        return originalHeight; // Don't modify other height values (from beam segments)
    }
}