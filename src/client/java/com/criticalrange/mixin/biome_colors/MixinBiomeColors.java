package com.criticalrange.mixin.biome_colors;

import com.criticalrange.VulkanModExtra;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Biome colors control mixin
 * Controls biome color rendering based on user preferences
 */
@Mixin(Biome.class)
public class MixinBiomeColors {

    @Inject(method = "getGrassColor", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlGrassColors(CallbackInfoReturnable<Integer> cir) {
        // Check if biome colors are disabled
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.detailSettings.biomeColors) {
            // Return a default grass color
            cir.setReturnValue(0x7FB238); // Default grass green
        }
    }

    @Inject(method = "getFoliageColor", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlFoliageColors(CallbackInfoReturnable<Integer> cir) {
        // Check if biome colors are disabled
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.detailSettings.biomeColors) {
            // Return a default foliage color
            cir.setReturnValue(0x59AE30); // Default foliage green
        }
    }

    @Inject(method = "getWaterColor", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlWaterColors(CallbackInfoReturnable<Integer> cir) {
        // Check if biome colors are disabled
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.detailSettings.biomeColors) {
            // Return a default water color
            cir.setReturnValue(0x3F76E4); // Default water blue
        }
    }
}
