package com.criticalrange.mixins.animations;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


/**
 * Texture animation control mixin based on Sodium Extra pattern
 * Controls individual texture animations for better performance
 * Each animation setting controls ONLY its own behavior (no master/global controls)
 * Removed all AND logic - individual toggles work independently
 */
@Mixin(SpriteAtlasTexture.class)
public abstract class MixinTextureAtlas extends AbstractTexture {
    

    @Redirect(method = "upload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/Sprite;createAnimation()Lnet/minecraft/client/texture/Sprite$TickableAnimation;"))
    public Sprite.TickableAnimation vulkanmodExtra$tickAnimatedSprites(Sprite instance) {
        Sprite.TickableAnimation tickableAnimation = instance.createAnimation();
        
        if (tickableAnimation != null) {
            String textureName = instance.getContents().getId().toString();
            boolean shouldAnimate = this.shouldAnimate(instance.getContents().getId());
            
            
            if (shouldAnimate) {
                return tickableAnimation;
            }
        }
        
        return null;
    }

    @Unique
    private boolean shouldAnimate(Identifier identifier) {
        if (identifier == null) {
            return true;
        }
        
        // Fast config access - no ConfigurationManager overhead
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.animationSettings != null) {
            var config = VulkanModExtra.CONFIG;

            // Cache string conversion to avoid repeated allocations
            String idString = identifier.getPath(); // More efficient than toString().toLowerCase()
            var settings = config.animationSettings;
            
            // Check master toggle first - if disabled, block all animations
            if (!settings.allAnimations) {
                return false;
            }
            
            // If master toggle is enabled, check individual settings
            
            // Fluid animations - each controls only its own behavior
            if (idString.contains("water_still")) return settings.waterStill;
            if (idString.contains("water_flow")) return settings.waterFlow;
            if (idString.contains("lava_still")) return settings.lavaStill;
            if (idString.contains("lava_flow")) return settings.lavaFlow;
            
            // Fire & light animations - each controls only its own behavior
            if (idString.contains("fire_0")) return settings.fire0;
            if (idString.contains("fire_1")) return settings.fire1;
            if (idString.contains("soul_fire_0")) return settings.soulFire0;
            if (idString.contains("soul_fire_1")) return settings.soulFire1;
            if (idString.contains("campfire_fire")) return settings.campfireFire;
            if (idString.contains("soul_campfire_fire")) return settings.soulCampfireFire;
            if (idString.contains("lantern") && !idString.contains("sea") && !idString.contains("soul")) return settings.lantern;
            if (idString.contains("soul_lantern")) return settings.soulLantern;
            if (idString.contains("sea_lantern")) return settings.seaLantern;
            
            // Portal animations - each controls only its own behavior
            if (idString.contains("nether_portal")) return settings.netherPortal;
            if (idString.contains("end_portal")) return settings.endPortal;
            if (idString.contains("end_gateway")) return settings.endGateway;
            
            // Block animations - each controls only its own behavior
            if (idString.contains("magma")) return settings.magma;
            if (idString.contains("prismarine_bricks")) return settings.prismarineBricks;
            if (idString.contains("dark_prismarine")) return settings.darkPrismarine;
            if (idString.contains("prismarine")) return settings.prismarine;
            if (idString.contains("conduit")) return settings.conduit;
            if (idString.contains("respawn_anchor")) return settings.respawnAnchor;
            if (idString.contains("stonecutter")) return settings.stonecutterSaw;
            
            // Machine animations (when active) - each controls only its own behavior
            if (idString.contains("blast_furnace_front_on")) return settings.blastFurnaceFrontOn;
            if (idString.contains("smoker_front_on")) return settings.smokerFrontOn;
            if (idString.contains("furnace_front_on")) return settings.furnaceFrontOn;
            
            // Plant animations - each controls only its own behavior
            if (idString.contains("kelp_plant")) return settings.kelpPlant;
            if (idString.contains("kelp")) return settings.kelp;
            if (idString.contains("tall_seagrass_bottom")) return settings.tallSeagrassBottom;
            if (idString.contains("tall_seagrass_top")) return settings.tallSeagrassTop;
            if (idString.contains("seagrass")) return settings.seagrass;
            
            // Nether stem animations - each controls only its own behavior
            if (idString.contains("warped_hyphae")) return settings.warpedHyphae;
            if (idString.contains("crimson_hyphae")) return settings.crimsonHyphae;
            if (idString.contains("warped_stem")) return settings.warpedStem;
            if (idString.contains("crimson_stem")) return settings.crimsonStem;
            
            // Sculk animations - each controls only its own behavior
            if (idString.contains("sculk_sensor_top")) return settings.sculkSensorTop;
            if (idString.contains("sculk_sensor_side")) return settings.sculkSensorSide;
            if (idString.contains("sculk_shrieker_top")) return settings.sculkShriekerTop;
            if (idString.contains("sculk_shrieker_side")) return settings.sculkShriekerSide;
            if (idString.contains("calibrated_sculk_sensor_top")) return settings.calibratedSculkSensorTop;
            if (idString.contains("calibrated_sculk_sensor_side")) return settings.calibratedSculkSensorSide;
            if (idString.contains("sculk_vein")) return settings.sculkVein;
            if (idString.contains("sculk_sensor")) return settings.sculkSensor;
            if (idString.contains("sculk_shrieker")) return settings.sculkShrieker;
            if (idString.contains("calibrated_sculk_sensor")) return settings.calibratedSculkSensor;
            if (idString.contains("sculk")) return settings.sculk;
            
            // Command block animations - each controls only its own behavior
            if (idString.contains("chain_command_block_front")) return settings.chainCommandBlockFront;
            if (idString.contains("repeating_command_block_front")) return settings.repeatingCommandBlockFront;
            if (idString.contains("command_block_front")) return settings.commandBlockFront;
            
            // Additional animations - each controls only its own behavior
            if (idString.contains("beacon")) return settings.beacon;
            if (idString.contains("dragon_egg")) return settings.dragonEgg;
            if (idString.contains("brewing_stand_base")) return settings.brewingStandBase;
            if (idString.contains("cauldron") && idString.contains("water")) return settings.cauldronWater;
            
        }

        // Default: allow animation for unrecognized textures
        return true;
    }
}
