package com.criticalrange.mixin.optimizations.beacon_beam_rendering;

import com.criticalrange.VulkanModExtra;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Beacon beam rendering optimization mixin
 * Controls beacon beam rendering and height limiting
 */
@Mixin(BeaconRenderer.class)
public class MixinBeaconRenderer {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$checkBeaconRendering(BeaconBlockEntity beaconBlockEntity, float partialTick, 
            PoseStack poseStack, MultiBufferSource multiBufferSource, 
            int packedLight, int packedOverlay, CallbackInfo ci) {
        // Cancel beacon beam rendering if disabled
        if (!VulkanModExtra.CONFIG.renderSettings.beaconBeam) {
            ci.cancel();
        }
        
        // Height limiting functionality
        if (VulkanModExtra.CONFIG.renderSettings.limitBeaconBeamHeight && beaconBlockEntity.getLevel() != null) {
            int worldHeight = beaconBlockEntity.getLevel().getMaxBuildHeight();
            int beaconY = beaconBlockEntity.getBlockPos().getY();
            
            // If beacon is too high and height limiting is enabled, don't render
            if (beaconY > worldHeight - 64) { // Limit beams near world height
                ci.cancel();
            }
        }
    }
}
