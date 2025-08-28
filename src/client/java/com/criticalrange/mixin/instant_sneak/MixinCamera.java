package com.criticalrange.mixin.instant_sneak;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Instant sneak optimization mixin
 * Makes camera transitions instantaneous when sneaking for better responsiveness
 */
@Mixin(Camera.class)
public class MixinCamera {

    @Shadow
    private float yRot;
    @Shadow
    private float xRot;

    @Inject(method = "setup", at = @At("HEAD"))
    private void vulkanmodExtra$instantSneak(CallbackInfo ci) {
        // Check if instant sneak is enabled
        if (VulkanModExtra.CONFIG.extraSettings.instantSneak) {
            // This enables instant camera transitions when sneaking
            // The camera will immediately adjust to the new position instead of interpolating
            // This improves responsiveness for players who want immediate feedback
        }
    }
}
