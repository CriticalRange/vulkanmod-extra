package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hierarchical fog control mixin for Minecraft 1.21.9+
 * 
 * IMPORTANT 1.21.9 CHANGE: Entity#getWorld() was renamed to Entity#getEntityWorld() in Yarn mappings
 * 
 * Uses multiple injection targets with require=0 for maximum compatibility.
 */
@Mixin(BackgroundRenderer.class)
public class MixinFogRenderer {

    /**
     * Primary fog control - targets the applyFog method that returns Fog
     */
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanmodExtra$controlFog(Camera camera, BackgroundRenderer.FogType fogType,
                                                   org.joml.Vector4f color, float viewDistance,
                                                   boolean thickFog, float tickDelta,
                                                   CallbackInfoReturnable<Fog> cir) {
        if (!shouldRenderFog(camera)) {
            cir.setReturnValue(Fog.DUMMY);
        }
    }

    /**
     * Alternative fog control - targets applyFog with different signature
     */
    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZ)V", 
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanmodExtra$controlFogAlt(Camera camera, BackgroundRenderer.FogType fogType,
                                                      float viewDistance, boolean thickFog,
                                                      CallbackInfo ci) {
        if (!shouldRenderFog(camera)) {
            ci.cancel();
        }
    }

    /**
     * Alternative fog control - targets void applyFog with tickDelta
     */
    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZF)V", 
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanmodExtra$controlFogAlt2(Camera camera, BackgroundRenderer.FogType fogType,
                                                       float viewDistance, boolean thickFog, float tickDelta,
                                                       CallbackInfo ci) {
        if (!shouldRenderFog(camera)) {
            ci.cancel();
        }
    }

    /**
     * Shared fog check logic - determines if fog should be rendered based on config
     * NOTE: Uses getEntityWorld() instead of getWorld() for 1.21.9+ Yarn mappings
     */
    private static boolean shouldRenderFog(Camera camera) {
        if (VulkanModExtra.CONFIG == null || VulkanModExtra.CONFIG.renderSettings == null) {
            return true; // Allow fog if config not loaded
        }

        var renderSettings = VulkanModExtra.CONFIG.renderSettings;

        // Master fog toggle - if disabled, block all fog
        if (!renderSettings.fog) {
            return false;
        }

        // Check dimension-specific and environment-specific settings
        Entity entity = camera.getFocusedEntity();
        if (entity == null) return true;

        BlockPos blockPos = entity.getBlockPos();
        // 1.21.9+: Entity#getWorld() renamed to Entity#getEntityWorld() in Yarn mappings
        World world = entity.getEntityWorld();

        // Check dimension-specific fog settings first
        try {
            Identifier dimensionId = world.getDimensionEntry().getKey().get().getValue();
            if (dimensionId.equals(DimensionTypes.OVERWORLD_ID) && !renderSettings.overworldFog) {
                return false;
            }
            if (dimensionId.equals(DimensionTypes.THE_NETHER_ID) && !renderSettings.netherFog) {
                return false;
            }
            if (dimensionId.equals(DimensionTypes.THE_END_ID) && !renderSettings.endFog) {
                return false;
            }
        } catch (Exception e) {
            // Dimension check failed, allow fog by default
        }

        // Check environment-specific fog settings (applies to all dimensions)
        FluidState fluidState = world.getFluidState(blockPos);

        // Check if entity is in water
        if (fluidState.isIn(FluidTags.WATER) && !renderSettings.waterFog) {
            return false;
        }

        // Check if entity is in lava
        if (fluidState.isIn(FluidTags.LAVA) && !renderSettings.lavaFog) {
            return false;
        }

        // Check if entity is in powder snow
        if (world.getBlockState(blockPos).isOf(Blocks.POWDER_SNOW) && !renderSettings.powderSnowFog) {
            return false;
        }

        return true;
    }
}