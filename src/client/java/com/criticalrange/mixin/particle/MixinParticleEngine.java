package com.criticalrange.mixin.particle;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Particle control mixin based on Sodium Extra pattern
 * Controls particle rendering for better performance
 */
@Mixin(ParticleEngine.class)
public class MixinParticleEngine {


    @Inject(method = "destroy", at = @At(value = "HEAD"), cancellable = true)
    public void vulkanmodExtra$controlBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        // Only cancel if both global particles and block break particles are disabled
        if (!VulkanModExtra.CONFIG.particleSettings.particles || !VulkanModExtra.CONFIG.particleSettings.blockBreak) {
            ci.cancel();
        }
    }

    @Inject(method = "crack", at = @At(value = "HEAD"), cancellable = true)
    public void vulkanmodExtra$controlBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        // Only cancel if both global particles and block breaking particles are disabled
        if (!VulkanModExtra.CONFIG.particleSettings.particles || !VulkanModExtra.CONFIG.particleSettings.blockBreaking) {
            ci.cancel();
        }
    }

    @Inject(method = "createParticle", at = @At(value = "HEAD"), cancellable = true)
    public void vulkanmodExtra$controlParticleCreation(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<Particle> cir) {
        // Early exit if particles are globally disabled
        if (!VulkanModExtra.CONFIG.particleSettings.particles) {
            cir.setReturnValue(null);
            return;
        }

        // Check specific particle type
        ResourceLocation particleTypeId = BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
        if (!VulkanModExtra.CONFIG.particleSettings.otherParticles.computeIfAbsent(particleTypeId.getPath(), k -> true)) {
            cir.setReturnValue(null);
        }
    }
}
