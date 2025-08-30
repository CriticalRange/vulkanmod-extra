package com.criticalrange.features.animation;

import com.criticalrange.core.BaseFeature;
import com.criticalrange.core.FeatureCategory;
import com.criticalrange.config.VulkanModExtraConfig;
import net.minecraft.client.Minecraft;

/**
 * Animation feature - controls various texture and block animations
 * Each animation type controls only its own behavior (no master control)
 */
public class AnimationFeature extends BaseFeature {

    public AnimationFeature() {
        super("animations", "Animations", FeatureCategory.ANIMATION,
              "Control texture animations, water, lava, fire, and other animated elements");
    }

    @Override
    public void initialize(Minecraft minecraft) {
        getLogger().info("Animation feature initialized");
    }

    @Override
    public boolean isEnabled() {
        VulkanModExtraConfig config = getConfig();
        return config != null && enabled;
    }

    /**
     * Check if a specific animation type is enabled
     */
    public boolean isAnimationEnabled(String animationName) {
        if (!isEnabled()) {
            return false;
        }

        VulkanModExtraConfig config = getConfig();
        if (config == null) {
            return true; // Default to enabled if no config
        }

        VulkanModExtraConfig.AnimationSettings settings = config.animationSettings;

        // Handle specific animation types - each controls only its own end
        return switch (animationName.toLowerCase()) {
            
            // Fluid animations
            case "water" -> settings.water;
            case "water_still" -> settings.waterStill;
            case "water_flow" -> settings.waterFlow;
            case "lava" -> settings.lava;
            case "lava_still" -> settings.lavaStill;
            case "lava_flow" -> settings.lavaFlow;
            
            // Fire & light animations
            case "fire" -> settings.fire;
            case "fire_0" -> settings.fire0;
            case "fire_1" -> settings.fire1;
            case "soul_fire" -> settings.soulFire;
            case "soul_fire_0" -> settings.soulFire0;
            case "soul_fire_1" -> settings.soulFire1;
            case "campfire_fire" -> settings.campfireFire;
            case "soul_campfire_fire" -> settings.soulCampfireFire;
            case "lantern" -> settings.lantern;
            case "soul_lantern" -> settings.soulLantern;
            case "sea_lantern" -> settings.seaLantern;
            
            // Portal animations
            case "portal" -> settings.portal;
            case "nether_portal" -> settings.netherPortal;
            case "end_portal" -> settings.endPortal;
            case "end_gateway" -> settings.endGateway;
            
            // Block animations
            case "block_animations" -> settings.blockAnimations;
            case "magma" -> settings.magma;
            case "prismarine" -> settings.prismarine;
            case "prismarine_bricks" -> settings.prismarineBricks;
            case "dark_prismarine" -> settings.darkPrismarine;
            case "conduit" -> settings.conduit;
            case "respawn_anchor" -> settings.respawnAnchor;
            case "stonecutter_saw" -> settings.stonecutterSaw;
            
            // Machine animations
            case "machine_animations" -> settings.machineAnimations;
            case "blast_furnace_front_on" -> settings.blastFurnaceFrontOn;
            case "smoker_front_on" -> settings.smokerFrontOn;
            case "furnace_front_on" -> settings.furnaceFrontOn;
            
            // Plant animations
            case "plant_animations" -> settings.plantAnimations;
            case "kelp" -> settings.kelp;
            case "kelp_plant" -> settings.kelpPlant;
            case "seagrass" -> settings.seagrass;
            case "tall_seagrass_bottom" -> settings.tallSeagrassBottom;
            case "tall_seagrass_top" -> settings.tallSeagrassTop;
            
            // Nether stem animations
            case "stem_animations" -> settings.stemAnimations;
            case "warped_stem" -> settings.warpedStem;
            case "crimson_stem" -> settings.crimsonStem;
            case "warped_hyphae" -> settings.warpedHyphae;
            case "crimson_hyphae" -> settings.crimsonHyphae;
            
            // Sculk animations
            case "sculk_animations" -> settings.sculkAnimations;
            case "sculk" -> settings.sculk;
            case "sculk_vein" -> settings.sculkVein;
            case "sculk_sensor" -> settings.sculkSensor;
            case "sculk_sensor_side" -> settings.sculkSensorSide;
            case "sculk_sensor_top" -> settings.sculkSensorTop;
            case "sculk_shrieker" -> settings.sculkShrieker;
            case "sculk_shrieker_side" -> settings.sculkShriekerSide;
            case "sculk_shrieker_top" -> settings.sculkShriekerTop;
            case "calibrated_sculk_sensor" -> settings.calibratedSculkSensor;
            case "calibrated_sculk_sensor_side" -> settings.calibratedSculkSensorSide;
            case "calibrated_sculk_sensor_top" -> settings.calibratedSculkSensorTop;
            
            // Command block animations
            case "command_block_animations" -> settings.commandBlockAnimations;
            case "command_block_front" -> settings.commandBlockFront;
            case "chain_command_block_front" -> settings.chainCommandBlockFront;
            case "repeating_command_block_front" -> settings.repeatingCommandBlockFront;
            
            // Additional animations
            case "additional_animations" -> settings.additionalAnimations;
            case "beacon" -> settings.beacon;
            case "dragon_egg" -> settings.dragonEgg;
            case "brewing_stand_base" -> settings.brewingStandBase;
            case "cauldron_water" -> settings.cauldronWater;
            
            default -> {
                // Default to enabled for unknown animations
                getLogger().debug("Unknown animation type: " + animationName + " - defaulting to enabled");
                yield true;
            }
        };
    }

    // Legacy compatibility methods
    public boolean isWaterAnimationEnabled() {
        return isAnimationEnabled("water");
    }

    public boolean isLavaAnimationEnabled() {
        return isAnimationEnabled("lava");
    }

    public boolean isFireAnimationEnabled() {
        return isAnimationEnabled("fire");
    }

    public boolean isPortalAnimationEnabled() {
        return isAnimationEnabled("portal");
    }

    public boolean isBlockAnimationEnabled() {
        return isAnimationEnabled("block_animations");
    }

    public boolean isTextureAnimationEnabled() {
        // Since master control is removed, texture animations are always enabled
        // Individual animation types control their own behavior
        return true;
    }

    public boolean isSculkSensorAnimationEnabled() {
        return isAnimationEnabled("sculk_sensor");
    }

    /**
     * Get animation settings for external use
     */
    public VulkanModExtraConfig.AnimationSettings getAnimationSettings() {
        VulkanModExtraConfig config = getConfig();
        return config != null ? config.animationSettings : new VulkanModExtraConfig.AnimationSettings();
    }
}
