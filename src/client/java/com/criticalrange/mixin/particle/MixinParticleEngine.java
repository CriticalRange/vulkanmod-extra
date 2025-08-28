package com.criticalrange.mixin.particle;

import com.criticalrange.client.config.VulkanModExtraClientConfig;
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
 * Mixin to control particle effects based on VulkanMod Extra settings
 * Based on sodium-extra's particle control system
 */
@Mixin(ParticleEngine.class)
public class MixinParticleEngine {
    @Inject(method = "destroy", at = @At(value = "HEAD"), cancellable = true)
    public void addBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!VulkanModExtraClientConfig.getInstance().particleSettings.particles ||
            !VulkanModExtraClientConfig.getInstance().particleSettings.blockBreak) {
            ci.cancel();
        }
    }

    @Inject(method = "crack", at = @At(value = "HEAD"), cancellable = true)
    public void addBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (!VulkanModExtraClientConfig.getInstance().particleSettings.particles ||
            !VulkanModExtraClientConfig.getInstance().particleSettings.blockBreaking) {
            ci.cancel();
        }
    }

    @Inject(method = "createParticle", at = @At(value = "HEAD"), cancellable = true)
    public void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<Particle> cir) {
        if (VulkanModExtraClientConfig.getInstance().particleSettings.particles) {
            ResourceLocation particleTypeId = BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
            // For now, we'll allow all particles since we haven't implemented the per-particle control yet
            // This can be expanded later to support individual particle type control
        } else {
            cir.setReturnValue(null);
        }
    }
}
