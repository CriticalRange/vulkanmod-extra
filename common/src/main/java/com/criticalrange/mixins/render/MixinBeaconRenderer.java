package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Beacon beam rendering optimization mixin
 * Controls beacon beam rendering and height limiting
 */
@Mixin(BeaconBlockEntityRenderer.class)
public class MixinBeaconRenderer {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$checkBeaconRendering(BeaconBlockEntity beaconBlockEntity, float partialTick,
            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
            int packedLight, int packedOverlay, CallbackInfo ci) {
        // Cancel beacon beam rendering if disabled
        if (!VulkanModExtra.CONFIG.renderSettings.beaconBeam) {
            ci.cancel();
        }

        // Height limiting functionality
        if (VulkanModExtra.CONFIG.renderSettings.limitBeaconBeamHeight && beaconBlockEntity.getWorld() != null) {
            int worldHeight = beaconBlockEntity.getWorld().getTopY();
            int beaconY = beaconBlockEntity.getPos().getY();

            // If beacon is too high and height limiting is enabled, don't render
            if (beaconY > worldHeight - 64) { // Limit beams near world height
                ci.cancel();
            }
        }
    }
}
