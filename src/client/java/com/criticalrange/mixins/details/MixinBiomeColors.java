package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.client.ConfigHelper;
import net.minecraft.world.biome.Biome;
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

    @Inject(method = "getGrassColorAt", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlGrassColors(double x, double z, CallbackInfoReturnable<Integer> cir) {
        // Check if biome colors are disabled
        if (ConfigHelper.isConfigInitialized() && !ConfigHelper.getConfig().detailSettings.biomeColors) {
            // Return a default grass color
            cir.setReturnValue(0x7FB238); // Default grass green
        }
    }

    @Inject(method = "getFoliageColor", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlFoliageColors(CallbackInfoReturnable<Integer> cir) {
        // Check if biome colors are disabled
        if (ConfigHelper.isConfigInitialized() && !ConfigHelper.getConfig().detailSettings.biomeColors) {
            // Return a default foliage color
            cir.setReturnValue(0x59AE30); // Default foliage green
        }
    }

    @Inject(method = "getWaterColor", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlWaterColors(CallbackInfoReturnable<Integer> cir) {
        // Check if biome colors are disabled
        if (ConfigHelper.isConfigInitialized() && !ConfigHelper.getConfig().detailSettings.biomeColors) {
            // Return a default water color
            cir.setReturnValue(0x3F76E4); // Default water blue
        }
    }
}
