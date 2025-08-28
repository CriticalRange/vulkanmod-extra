package com.criticalrange.mixin.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Performance-focused particle optimization mixin
 * Reduces expensive particle effects for better performance
 */
@Mixin(ParticleEngine.class)
public class MixinParticleEngine {

    // Counter to throttle expensive particles
    private static int particleThrottleCounter = 0;

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$optimizeParticleCreation(ParticleOptions particleOptions, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> cir) {
        // Throttle expensive particle effects for performance
        if (shouldThrottleParticle(particleOptions)) {
            particleThrottleCounter++;
            // Only allow every 3rd expensive particle to spawn
            if (particleThrottleCounter % 3 != 0) {
                cir.setReturnValue(null);
                return;
            }
        }

        // Reset counter periodically to prevent overflow
        if (particleThrottleCounter > 1000) {
            particleThrottleCounter = 0;
        }
    }

    private static boolean shouldThrottleParticle(ParticleOptions particleOptions) {
        // Throttle the most performance-intensive particle types
        return particleOptions.getType() == ParticleTypes.FLAME ||
               particleOptions.getType() == ParticleTypes.SMOKE ||
               particleOptions.getType() == ParticleTypes.CAMPFIRE_COSY_SMOKE ||
               particleOptions.getType() == ParticleTypes.CAMPFIRE_SIGNAL_SMOKE ||
               particleOptions.getType() == ParticleTypes.BUBBLE ||
               particleOptions.getType() == ParticleTypes.BUBBLE_POP ||
               particleOptions.getType() == ParticleTypes.SPLASH ||
               particleOptions.getType() == ParticleTypes.RAIN ||
               particleOptions.getType() == ParticleTypes.DRIPPING_WATER ||
               particleOptions.getType() == ParticleTypes.FALLING_WATER;
    }
}
