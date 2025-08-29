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
    public void vulkanmodExtra$addBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.particleSettings.particles || !VulkanModExtra.CONFIG.particleSettings.blockBreak) {
            ci.cancel();
        }
    }

    @Inject(method = "crack", at = @At(value = "HEAD"), cancellable = true)
    public void vulkanmodExtra$addBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.particleSettings.particles || !VulkanModExtra.CONFIG.particleSettings.blockBreaking) {
            ci.cancel();
        }
    }

    @Inject(method = "createParticle", at = @At(value = "HEAD"), cancellable = true)
    public void vulkanmodExtra$addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<Particle> cir) {
        if (VulkanModExtra.CONFIG.particleSettings.particles) {
            ResourceLocation particleTypeId = BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
            if (!VulkanModExtra.CONFIG.particleSettings.otherMap.computeIfAbsent(particleTypeId, k -> true)) {
                cir.setReturnValue(null);
            }
        } else {
            cir.setReturnValue(null);
        }
    }
}
