package com.criticalrange.mixin.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Performance-focused particle optimization mixin
 * Reduces expensive particle effects for better performance
 */
@Mixin(ParticleEngine.class)
public class MixinParticleEngine {

    // Performance optimization - reduce particle processing overhead
    @Inject(method = "tick", at = @At("HEAD"))
    private void vulkanmodExtra$optimizeParticleTicks(CallbackInfo ci) {
        // Framework for particle tick optimizations
        // Could implement particle batching, culling, or lifecycle optimizations
    }

    // Add particle creation control using config settings
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$controlParticleCreation(ParticleOptions particleOptions, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> cir) {
        // Check if particles are enabled in config
        if (!com.criticalrange.VulkanModExtra.CONFIG.particleSettings.particles) {
            cir.setReturnValue(null);
            return;
        }

        // Check specific particle type settings
        if (shouldDisableParticle(particleOptions)) {
            cir.setReturnValue(null);
            return;
        }
    }

    private boolean shouldDisableParticle(ParticleOptions particleOptions) {
        // Check config settings for specific particle types
        if (particleOptions.getType() == ParticleTypes.FLAME && !com.criticalrange.VulkanModExtra.CONFIG.particleSettings.flame) {
            return true;
        }
        if (particleOptions.getType() == ParticleTypes.SMOKE && !com.criticalrange.VulkanModExtra.CONFIG.particleSettings.smoke) {
            return true;
        }
        if ((particleOptions.getType() == ParticleTypes.CAMPFIRE_COSY_SMOKE || particleOptions.getType() == ParticleTypes.CAMPFIRE_SIGNAL_SMOKE)
            && !com.criticalrange.VulkanModExtra.CONFIG.particleSettings.campfireCosySmoke) {
            return true;
        }
        if (particleOptions.getType() == ParticleTypes.BUBBLE && !com.criticalrange.VulkanModExtra.CONFIG.particleSettings.bubble) {
            return true;
        }
        if (particleOptions.getType() == ParticleTypes.SPLASH && !com.criticalrange.VulkanModExtra.CONFIG.particleSettings.splash) {
            return true;
        }
        if (particleOptions.getType() == ParticleTypes.RAIN && !com.criticalrange.VulkanModExtra.CONFIG.particleSettings.rain) {
            return true;
        }
        if (particleOptions.getType() == ParticleTypes.DRIPPING_WATER && !com.criticalrange.VulkanModExtra.CONFIG.particleSettings.drippingWater) {
            return true;
        }
        return false;
    }


}
