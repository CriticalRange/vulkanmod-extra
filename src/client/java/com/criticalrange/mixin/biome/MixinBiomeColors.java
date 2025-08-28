package com.criticalrange.mixin.biome;

import com.criticalrange.client.config.VulkanModExtraClientConfig;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to control biome color rendering based on VulkanMod Extra settings
 * Based on sodium-extra's biome colors control system
 */
@Mixin(Biome.class)
public class MixinBiomeColors {
    @Inject(method = "getGrassColor", at = @At("HEAD"), cancellable = true)
    private void getGrassColor(double x, double z, CallbackInfoReturnable<Integer> cir) {
        if (!VulkanModExtraClientConfig.getInstance().detailSettings.biomeColors) {
            cir.setReturnValue(0xFF8DB360); // Default grass color
        }
    }

    @Inject(method = "getFoliageColor", at = @At("HEAD"), cancellable = true)
    private void getFoliageColor(CallbackInfoReturnable<Integer> cir) {
        if (!VulkanModExtraClientConfig.getInstance().detailSettings.biomeColors) {
            cir.setReturnValue(0xFF71A74D); // Default foliage color
        }
    }
}
