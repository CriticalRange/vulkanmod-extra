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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hierarchical fog control mixin for Minecraft 1.21.2+
 * Uses the new applyFog method that returns a Fog object
 * 1. Master fog toggle overrides all
 * 2. Dimension-specific fog toggles (Overworld, Nether, End)
 * 3. Environment-specific toggles (water, lava, powder snow)
 */
@Mixin(BackgroundRenderer.class)
public class MixinFogRenderer {

    /**
     * Hierarchical fog control system for 1.21.2+ using the new applyFog method signature
     * Method signature: applyFog(Camera, FogType, Vector4f, float, boolean, float) -> Fog
     * We intercept the method and return Fog.DUMMY when fog is disabled
     */
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void vulkanmodExtra$controlFog(Camera camera, BackgroundRenderer.FogType fogType,
                                                   org.joml.Vector4f color, float viewDistance,
                                                   boolean thickFog, float tickDelta,
                                                   CallbackInfoReturnable<Fog> cir) {
        if (VulkanModExtra.CONFIG == null || VulkanModExtra.CONFIG.renderSettings == null) {
            return;
        }

        var renderSettings = VulkanModExtra.CONFIG.renderSettings;

        // Master fog toggle - if disabled, return dummy fog (effectively disabling fog)
        if (!renderSettings.fog) {
            cir.setReturnValue(Fog.DUMMY);
            return;
        }

        // Master fog is enabled, check dimension-specific and environment-specific settings
        Entity entity = camera.getFocusedEntity();
        if (entity == null) return;

        BlockPos blockPos = entity.getBlockPos();
        World world = entity.getWorld();

        // Check dimension-specific fog settings first
        Identifier dimensionId = world.getDimensionEntry().getKey().get().getValue();
        if (dimensionId.equals(DimensionTypes.OVERWORLD_ID) && !renderSettings.overworldFog) {
            cir.setReturnValue(Fog.DUMMY);
            return;
        }
        if (dimensionId.equals(DimensionTypes.THE_NETHER_ID) && !renderSettings.netherFog) {
            cir.setReturnValue(Fog.DUMMY);
            return;
        }
        if (dimensionId.equals(DimensionTypes.THE_END_ID) && !renderSettings.endFog) {
            cir.setReturnValue(Fog.DUMMY);
            return;
        }

        // Check environment-specific fog settings (applies to all dimensions)
        FluidState fluidState = world.getFluidState(blockPos);

        // Check if entity is in water
        if (fluidState.isIn(FluidTags.WATER) && !renderSettings.waterFog) {
            cir.setReturnValue(Fog.DUMMY);
            return;
        }

        // Check if entity is in lava
        if (fluidState.isIn(FluidTags.LAVA) && !renderSettings.lavaFog) {
            cir.setReturnValue(Fog.DUMMY);
            return;
        }

        // Check if entity is in powder snow
        if (world.getBlockState(blockPos).isOf(Blocks.POWDER_SNOW) && !renderSettings.powderSnowFog) {
            cir.setReturnValue(Fog.DUMMY);
            return;
        }
    }
}