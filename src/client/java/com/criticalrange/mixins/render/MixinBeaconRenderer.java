package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.util.VersionHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Beacon beam rendering optimization mixin
 * Controls beacon beam rendering and height limiting
 * Uses MixinExtras for version-conditional method handling
 */
@Mixin(BeaconBlockEntityRenderer.class)
public class MixinBeaconRenderer {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$checkBeaconRendering(BeaconBlockEntity beaconBlockEntity, float partialTick,
            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
            int packedLight, int packedOverlay, CallbackInfo ci) {
        // Cancel beacon beam rendering if disabled
        if (!VulkanModExtra.CONFIG.renderSettings.beaconBeam) {
            ci.cancel();
        }

        // Height limiting functionality with version-conditional method calls
        if (VulkanModExtra.CONFIG.renderSettings.limitBeaconBeamHeight && beaconBlockEntity.getWorld() != null) {
            int worldHeight = getWorldTopY(beaconBlockEntity.getWorld(), beaconBlockEntity.getPos().getX(), beaconBlockEntity.getPos().getZ());
            int beaconY = beaconBlockEntity.getPos().getY();

            // If beacon is too high and height limiting is enabled, don't render
            if (beaconY > worldHeight - 64) { // Limit beams near world height
                ci.cancel();
            }
        }
    }

    /**
     * Version-conditional method to get world top Y coordinate
     * Handles method signature differences between Minecraft versions
     */
    private int getWorldTopY(World world, int x, int z) {
        if (VersionHelper.IS_POST_1_21_1) {
            // 1.21.2+ version: getTopY(Heightmap.Type, int, int)
            return world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        } else {
            // 1.21.1 version: getTopY() - simplified for compatibility
            try {
                // Fallback to general getTopY() method
                return world.getTopY();
            } catch (Exception e) {
                // If all else fails, use world height
                return world.getHeight();
            }
        }
    }

    /**
     * Wrap operations that might have version-specific differences
     * This ensures we handle method calls that change between versions
     */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTopY()I", remap = false))
    private int vulkanmodExtra$wrapGetTopY(World world, Operation<Integer> original) {
        // Only wrap if the method signature is different in current version
        if (VersionHelper.IS_POST_1_21_1) {
            // For 1.21.2+, we need to provide additional parameters
            // Since we can't determine coordinates from context, use center chunk
            return world.getTopY(Heightmap.Type.WORLD_SURFACE, 0, 0);
        } else {
            // For 1.21.1, call original method
            return original.call(world);
        }
    }
}
