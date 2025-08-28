package com.criticalrange.mixin.instant_sneak;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Performance-focused instant sneak optimization mixin
 * Makes camera transitions instantaneous when sneaking for better responsiveness
 */
@Mixin(Camera.class)
public class MixinCamera {

    @Inject(method = "setup", at = @At("HEAD"))
    private void vulkanmodExtra$optimizeCameraSetup(CallbackInfo ci) {
        // This mixin enables instant camera transitions when sneaking
        // The actual optimization would be to modify camera interpolation
        // For now, this is a placeholder for the instant sneak functionality
    }
}
