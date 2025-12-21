package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Efficient beacon beam height control mixin for Minecraft 1.21.5
 * Updated for new render signature with Vec3d camera position parameter
 * BeaconRenderer now uses generic type T extends BlockEntity & BeaconBeamOwner
 */
@Mixin(BeaconBlockEntityRenderer.class)
public class MixinBeaconRenderer {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$checkBeaconRendering(BlockEntity blockEntity, float partialTick,
            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
            int packedLight, int packedOverlay, Vec3d cameraPos, CallbackInfo ci) {
        // Cancel beacon beam rendering if disabled
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null &&
            !VulkanModExtra.CONFIG.renderSettings.beaconBeam) {
            ci.cancel();
        }
    }

    /**
     * Efficiently modify beacon beam height by intercepting the maxY value
     * In 1.21.5, renderBeam signature changed to include 'float scale' parameter
     * New signature: renderBeam(MatrixStack, VertexConsumerProvider, float tickProgress, float scale, long worldTime, int yOffset, int maxY, int color)
     * 
     * Note: We intercept ALL maxY values, not just hardcoded 1024, to ensure it works regardless of the default value
     */
    @ModifyArg(method = "render", at = @At(value = "INVOKE",
               target = "Lnet/minecraft/client/render/block/entity/BeaconBlockEntityRenderer;renderBeam(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;FFJIII)V"),
               index = 6) // maxY parameter (7th parameter, 0-indexed = 6)
    private int vulkanmodExtra$modifyBeaconHeight(int originalHeight) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            int configuredHeight = VulkanModExtra.CONFIG.renderSettings.beaconBeamHeight;
            // Only modify if it's a large value (top segment), not segment heights
            // Segment heights are typically small (< 100), while max height is 1024 or similar
            if (originalHeight >= 256) {
                // Clamp to valid range (32-512)
                return Math.max(32, Math.min(512, configuredHeight));
            }
        }
        return originalHeight; // Don't modify small height values (from beam segments)
    }
}