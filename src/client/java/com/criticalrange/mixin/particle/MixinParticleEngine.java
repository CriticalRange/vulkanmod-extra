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
import org.spongepowered.asm.mixin.Unique;
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
        try {
            com.criticalrange.config.ConfigurationManager configManager = com.criticalrange.config.ConfigurationManager.getInstance();
            com.criticalrange.config.VulkanModExtraConfig config = configManager.getConfig();
            
            // Check master toggle first, then individual setting
            if (!config.particleSettings.allParticles || !config.particleSettings.blockBreak) {
                ci.cancel();
            }
        } catch (Exception ex) {
            VulkanModExtra.LOGGER.error("Failed to get particle config in mixin", ex);
        }
    }

    @Inject(method = "crack", at = @At(value = "HEAD"), cancellable = true)
    public void vulkanmodExtra$controlBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        try {
            com.criticalrange.config.ConfigurationManager configManager = com.criticalrange.config.ConfigurationManager.getInstance();
            com.criticalrange.config.VulkanModExtraConfig config = configManager.getConfig();
            
            // Check master toggle first, then individual setting
            if (!config.particleSettings.allParticles || !config.particleSettings.blockBreaking) {
                ci.cancel();
            }
        } catch (Exception ex) {
            VulkanModExtra.LOGGER.error("Failed to get particle config in mixin", ex);
        }
    }

    @Inject(method = "createParticle", at = @At(value = "HEAD"), cancellable = true)
    public void vulkanmodExtra$controlParticleCreation(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<Particle> cir) {
        try {
            com.criticalrange.config.ConfigurationManager configManager = com.criticalrange.config.ConfigurationManager.getInstance();
            com.criticalrange.config.VulkanModExtraConfig config = configManager.getConfig();
            
            // Get particle type ID
            ResourceLocation particleTypeId = BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
            String particleName = particleTypeId.getPath();
            
            // Check specific particle type settings
            boolean shouldRender = shouldRenderParticle(config, particleName);
            if (!shouldRender) {
                cir.setReturnValue(null);
            }
        } catch (Exception ex) {
            VulkanModExtra.LOGGER.error("Failed to get particle config in mixin", ex);
        }
    }

    @Unique
    private boolean shouldRenderParticle(com.criticalrange.config.VulkanModExtraConfig config, String particleName) {
        var settings = config.particleSettings;
        
        // Check master toggle first - if disabled, block all particles
        if (!settings.allParticles) {
            return false;
        }
        
        // If master toggle is enabled, check individual settings
        return switch (particleName.toLowerCase()) {
            case "ambient_entity_effect" -> settings.ambientEntityEffect;
            case "angry_villager" -> settings.angryVillager;
            case "ash" -> settings.ash;
            case "barrier" -> settings.barrier;
            case "block" -> settings.block;
            case "blockdust" -> settings.blockdust;
            case "block_marker" -> settings.blockMarker;
            case "bubble" -> settings.bubble;
            case "bubble_column_up" -> settings.bubbleColumnUp;
            case "bubble_pop" -> settings.bubblePop;
            case "campfire_cosy_smoke" -> settings.campfireCosySmoke;
            case "campfire_signal_smoke" -> settings.campfireSignalSmoke;
            case "cherry_leaves" -> settings.cherryLeaves;
            case "cloud" -> settings.cloud;
            case "composter" -> settings.composter;
            case "crimson_spore" -> settings.crimsonSpore;
            case "crit" -> settings.crit;
            case "current_down" -> settings.currentDown;
            case "damage_indicator" -> settings.damageIndicator;
            case "dolphin" -> settings.dolphin;
            case "dragon_breath" -> settings.dragonBreath;
            case "dripping_dripstone_lava" -> settings.drippingDripstoneLava;
            case "dripping_dripstone_water" -> settings.drippingDripstoneWater;
            case "dripping_honey" -> settings.drippingHoney;
            case "dripping_lava" -> settings.drippingLava;
            case "dripping_obsidian_tear" -> settings.drippingObsidianTear;
            case "dripping_water" -> settings.drippingWater;
            case "dust" -> settings.dust;
            case "dust_color_transition" -> settings.dustColorTransition;
            case "dust_pillar" -> settings.dustPillar;
            case "dust_plume" -> settings.dustPlume;
            case "effect" -> settings.effect;
            case "egg_crack" -> settings.eggCrack;
            case "elder_guardian" -> settings.elderGuardian;
            case "electric_spark" -> settings.electricSpark;
            case "enchant" -> settings.enchant;
            case "enchanted_hit" -> settings.enchantedHit;
            case "end_rod" -> settings.endRod;
            case "entity_effect" -> settings.entityEffect;
            case "explosion" -> settings.explosion;
            case "explosion_emitter" -> settings.explosionEmitter;
            case "falling_dripstone_lava" -> settings.fallingDripstoneLava;
            case "falling_dripstone_water" -> settings.fallingDripstoneWater;
            case "falling_dust" -> settings.fallingDust;
            case "falling_honey" -> settings.fallingHoney;
            case "falling_lava" -> settings.fallingLava;
            case "falling_nectar" -> settings.fallingNectar;
            case "falling_obsidian_tear" -> settings.fallingObsidianTear;
            case "falling_spore_blossom" -> settings.fallingSporeBlossom;
            case "falling_water" -> settings.fallingWater;
            case "firework" -> settings.firework;
            case "fishing" -> settings.fishing;
            case "flame" -> settings.flame;
            case "flash" -> settings.flash;
            case "glow" -> settings.glow;
            case "glow_squid_ink" -> settings.glowSquidInk;
            case "gust" -> settings.gust;
            case "gust_emitter_large" -> settings.gustEmitterLarge;
            case "gust_emitter_small" -> settings.gustEmitterSmall;
            case "happy_villager" -> settings.happyVillager;
            case "heart" -> settings.heart;
            case "infested" -> settings.infested;
            case "instant_effect" -> settings.instantEffect;
            case "item" -> settings.item;
            case "item_cobweb" -> settings.itemCobweb;
            case "item_slime" -> settings.itemSlime;
            case "item_snowball" -> settings.itemSnowball;
            case "landing_honey" -> settings.landingHoney;
            case "landing_lava" -> settings.landingLava;
            case "landing_obsidian_tear" -> settings.landingObsidianTear;
            case "large_smoke" -> settings.largeSmoke;
            case "lava" -> settings.lava;
            case "light_block" -> settings.lightBlock;
            case "mycelium" -> settings.mycelium;
            case "nautilus" -> settings.nautilus;
            case "note" -> settings.note;
            case "ominous_spawning" -> settings.ominousSpawning;
            case "poof" -> settings.poof;
            case "portal" -> settings.portal;
            case "raid_omen" -> settings.raidOmen;
            case "rain" -> settings.rain;
            case "reverse_portal" -> settings.reversePortal;
            case "scrape" -> settings.scrape;
            case "sculk_charge" -> settings.sculkCharge;
            case "sculk_charge_pop" -> settings.sculkChargePop;
            case "sculk_soul" -> settings.sculkSoul;
            case "sculk_shrieker", "shriek" -> settings.shriek;
            case "small_flame" -> settings.smallFlame;
            case "small_gust" -> settings.smallGust;
            case "smoke" -> settings.smoke;
            case "sneeze" -> settings.sneeze;
            case "snowflake" -> settings.snowflake;
            case "sonic_boom" -> settings.sonicBoom;
            case "soul" -> settings.soul;
            case "soul_fire_flame" -> settings.soulFireFlame;
            case "spit" -> settings.spit;
            case "splash" -> settings.splash;
            case "spore_blossom_air" -> settings.sporeBlossomAir;
            case "squid_ink" -> settings.squidInk;
            case "sweep_attack" -> settings.sweepAttack;
            case "totem_of_undying" -> settings.totemOfUndying;
            case "trail" -> settings.trail;
            case "trial_omen" -> settings.trialOmen;
            case "trial_spawner_detection" -> settings.trialSpawnerDetection;
            case "trial_spawner_detection_ominous" -> settings.trialSpawnerDetectionOminous;
            case "underwater" -> settings.underwater;
            case "vault_connection" -> settings.vaultConnection;
            case "vibration" -> settings.vibration;
            case "warped_spore" -> settings.warpedSpore;
            case "wax_off" -> settings.waxOff;
            case "wax_on" -> settings.waxOn;
            case "white_ash" -> settings.whiteAsh;
            case "white_smoke" -> settings.whiteSmoke;
            case "witch" -> settings.witch;
            case "wither" -> settings.wither;
            case "wither_armor" -> settings.witherArmor;
            default -> {
                // Check if it's in the otherParticles map
                Boolean customSetting = settings.otherParticles.get(particleName);
                if (customSetting != null) {
                    yield customSetting;
                }
                // Log unknown particles for debugging
                VulkanModExtra.LOGGER.debug("Unknown particle type: " + particleName + " - defaulting to enabled");
                yield true; // Default: allow unknown particles
            }
        };
    }
}
