package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
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

/**
 * Hierarchical fog control mixin
 * 1. Master fog toggle overrides all
 * 2. Dimension-specific fog toggles (Overworld, Nether, End)
 * 3. Environment-specific toggles (water, lava, powder snow)
 */
@Mixin(BackgroundRenderer.class)
public class MixinFogRenderer {

    /**
     * Hierarchical fog control system
     * 1. Master fog toggle - if disabled, cancels ALL fog
     * 2. Individual fog toggles - if master enabled, checks specific fog type
     */
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanmodExtra$controlFog(Camera camera, BackgroundRenderer.FogType fogType,
                                                   float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG == null || VulkanModExtra.CONFIG.renderSettings == null) {
            return;
        }

        var renderSettings = VulkanModExtra.CONFIG.renderSettings;

        // Master fog toggle - if disabled, cancel ALL fog
        if (!renderSettings.fog) {
            ci.cancel();
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
            ci.cancel();
            return;
        }
        if (dimensionId.equals(DimensionTypes.THE_NETHER_ID) && !renderSettings.netherFog) {
            ci.cancel();
            return;
        }
        if (dimensionId.equals(DimensionTypes.THE_END_ID) && !renderSettings.endFog) {
            ci.cancel();
            return;
        }

        // Check environment-specific fog settings (applies to all dimensions)
        FluidState fluidState = world.getFluidState(blockPos);

        // Check if entity is in water
        if (fluidState.isIn(FluidTags.WATER) && !renderSettings.waterFog) {
            ci.cancel();
            return;
        }

        // Check if entity is in lava
        if (fluidState.isIn(FluidTags.LAVA) && !renderSettings.lavaFog) {
            ci.cancel();
            return;
        }

        // Check if entity is in powder snow
        if (world.getBlockState(blockPos).isOf(Blocks.POWDER_SNOW) && !renderSettings.powderSnowFog) {
            ci.cancel();
            return;
        }
    }
}
