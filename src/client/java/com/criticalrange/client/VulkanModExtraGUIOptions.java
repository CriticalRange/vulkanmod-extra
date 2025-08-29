package com.criticalrange.client;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.client.config.VulkanModExtraClientConfig;
import net.minecraft.world.level.material.FogType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Runtime GUI options provider for VulkanMod Extra
 * This class dynamically creates VulkanMod-compatible option blocks at runtime
 */
public class VulkanModExtraGUIOptions {

    private static Class<?> optionBlockClass;
    private static Class<?> switchOptionClass;
    private static Class<?> rangeOptionClass;
    private static Class<?> cyclingOptionClass;
    private static Class<?> componentClass;

    private static boolean classesLoaded = false;

    /**
     * Initialize the GUI options system by loading VulkanMod classes
     */
    public static boolean initializeClasses() {
        if (classesLoaded) {
            return true;
        }

        try {
            // Load VulkanMod classes dynamically
            optionBlockClass = Class.forName("net.vulkanmod.config.option.OptionBlock");
            switchOptionClass = Class.forName("net.vulkanmod.config.option.SwitchOption");
            rangeOptionClass = Class.forName("net.vulkanmod.config.option.RangeOption");
            cyclingOptionClass = Class.forName("net.vulkanmod.config.option.CyclingOption");
            componentClass = Class.forName("net.minecraft.network.chat.Component");

            classesLoaded = true;
            VulkanModExtra.LOGGER.info("Successfully loaded VulkanMod GUI classes");
            return true;

        } catch (ClassNotFoundException e) {
            VulkanModExtra.LOGGER.warn("Could not load VulkanMod GUI classes: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create animation options block
     */
    public static Object[] getAnimationOpts() {
        if (!initializeClasses()) {
            return new Object[0];
        }

        try {
            List<Object> options = new ArrayList<>();

            // Create SwitchOption for animation master toggle
            Object animationOption = createSwitchOption(
                "vulkanmod-extra.option.animation",
                () -> VulkanModExtraClientConfig.getInstance().animationSettings.animation,
                value -> VulkanModExtraClientConfig.getInstance().animationSettings.animation = value,
                "vulkanmod-extra.option.animation.tooltip"
            );
            options.add(animationOption);

            // Create SwitchOption for water animation
            Object waterOption = createSwitchOption(
                "vulkanmod-extra.option.water",
                () -> VulkanModExtraClientConfig.getInstance().animationSettings.water,
                value -> VulkanModExtraClientConfig.getInstance().animationSettings.water = value,
                "vulkanmod-extra.option.water.tooltip"
            );
            options.add(waterOption);

            // Create SwitchOption for lava animation
            Object lavaOption = createSwitchOption(
                "vulkanmod-extra.option.lava",
                () -> VulkanModExtraClientConfig.getInstance().animationSettings.lava,
                value -> VulkanModExtraClientConfig.getInstance().animationSettings.lava = value,
                "vulkanmod-extra.option.lava.tooltip"
            );
            options.add(lavaOption);

            // Create SwitchOption for fire animation
            Object fireOption = createSwitchOption(
                "vulkanmod-extra.option.fire",
                () -> VulkanModExtraClientConfig.getInstance().animationSettings.fire,
                value -> VulkanModExtraClientConfig.getInstance().animationSettings.fire = value,
                "vulkanmod-extra.option.fire.tooltip"
            );
            options.add(fireOption);

            // Create SwitchOption for portal animation
            Object portalOption = createSwitchOption(
                "vulkanmod-extra.option.portal",
                () -> VulkanModExtraClientConfig.getInstance().animationSettings.portal,
                value -> VulkanModExtraClientConfig.getInstance().animationSettings.portal = value,
                "vulkanmod-extra.option.portal.tooltip"
            );
            options.add(portalOption);

            // Create SwitchOption for block animations
            Object blockAnimOption = createSwitchOption(
                "vulkanmod-extra.option.block_animations",
                () -> VulkanModExtraClientConfig.getInstance().animationSettings.blockAnimations,
                value -> VulkanModExtraClientConfig.getInstance().animationSettings.blockAnimations = value,
                "vulkanmod-extra.option.block_animations.tooltip"
            );
            options.add(blockAnimOption);

            // Create SwitchOption for sculk sensor animation
            Object sculkOption = createSwitchOption(
                "vulkanmod-extra.option.sculk_sensor",
                () -> VulkanModExtraClientConfig.getInstance().animationSettings.sculkSensor,
                value -> VulkanModExtraClientConfig.getInstance().animationSettings.sculkSensor = value,
                "vulkanmod-extra.option.sculk_sensor.tooltip"
            );
            options.add(sculkOption);

            // Create option block with animation options
            Object animationBlock = createOptionBlock("vulkanmod-extra.category.animation", options.toArray());
            return new Object[]{animationBlock};

        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create animation options", e);
            return new Object[0];
        }
    }

    /**
     * Create particle options block
     */
    public static Object[] getParticleOpts() {
        if (!initializeClasses()) {
            return new Object[0];
        }

        try {
            List<Object> options = new ArrayList<>();

            // Particles master toggle
            Object particlesOption = createSwitchOption(
                "vulkanmod-extra.option.particles",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.particles,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.particles = value,
                "vulkanmod-extra.option.particles.tooltip"
            );
            options.add(particlesOption);

            // Rain splash particles
            Object rainSplashOption = createSwitchOption(
                "vulkanmod-extra.option.rain_splash",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.rainSplash,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.rainSplash = value,
                "vulkanmod-extra.option.rain_splash.tooltip"
            );
            options.add(rainSplashOption);

            // Block break particles
            Object blockBreakOption = createSwitchOption(
                "vulkanmod-extra.option.block_break",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.blockBreak,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.blockBreak = value,
                "vulkanmod-extra.option.block_break.tooltip"
            );
            options.add(blockBreakOption);

            // Block breaking particles
            Object blockBreakingOption = createSwitchOption(
                "vulkanmod-extra.option.block_breaking",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.blockBreaking,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.blockBreaking = value,
                "vulkanmod-extra.option.block_breaking.tooltip"
            );
            options.add(blockBreakingOption);

            // Common particle types
            Object flameOption = createSwitchOption(
                "vulkanmod-extra.option.flame",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.flame,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.flame = value,
                "vulkanmod-extra.option.flame.tooltip"
            );
            options.add(flameOption);

            Object smokeOption = createSwitchOption(
                "vulkanmod-extra.option.smoke",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.smoke,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.smoke = value,
                "vulkanmod-extra.option.smoke.tooltip"
            );
            options.add(smokeOption);

            Object bubbleOption = createSwitchOption(
                "vulkanmod-extra.option.bubble",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.bubble,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.bubble = value,
                "vulkanmod-extra.option.bubble.tooltip"
            );
            options.add(bubbleOption);

            Object splashOption = createSwitchOption(
                "vulkanmod-extra.option.splash",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.splash,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.splash = value,
                "vulkanmod-extra.option.splash.tooltip"
            );
            options.add(splashOption);

            Object rainOption = createSwitchOption(
                "vulkanmod-extra.option.rain",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.rain,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.rain = value,
                "vulkanmod-extra.option.rain.tooltip"
            );
            options.add(rainOption);

            Object drippingWaterOption = createSwitchOption(
                "vulkanmod-extra.option.drippingWater",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.drippingWater,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.drippingWater = value,
                "vulkanmod-extra.option.drippingWater.tooltip"
            );
            options.add(drippingWaterOption);

            Object explosionOption = createSwitchOption(
                "vulkanmod-extra.option.explosion",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.explosion,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.explosion = value,
                "vulkanmod-extra.option.explosion.tooltip"
            );
            options.add(explosionOption);

            Object heartOption = createSwitchOption(
                "vulkanmod-extra.option.heart",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.heart,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.heart = value,
                "vulkanmod-extra.option.heart.tooltip"
            );
            options.add(heartOption);

            Object critOption = createSwitchOption(
                "vulkanmod-extra.option.crit",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.crit,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.crit = value,
                "vulkanmod-extra.option.crit.tooltip"
            );
            options.add(critOption);

            Object enchantOption = createSwitchOption(
                "vulkanmod-extra.option.enchant",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.enchant,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.enchant = value,
                "vulkanmod-extra.option.enchant.tooltip"
            );
            options.add(enchantOption);

            Object noteOption = createSwitchOption(
                "vulkanmod-extra.option.note",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.note,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.note = value,
                "vulkanmod-extra.option.note.tooltip"
            );
            options.add(noteOption);

            Object lavaOption = createSwitchOption(
                "vulkanmod-extra.option.lava",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.lava,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.lava = value,
                "vulkanmod-extra.option.lava.tooltip"
            );
            options.add(lavaOption);

            Object fireworkOption = createSwitchOption(
                "vulkanmod-extra.option.firework",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.firework,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.firework = value,
                "vulkanmod-extra.option.firework.tooltip"
            );
            options.add(fireworkOption);

            Object happyVillagerOption = createSwitchOption(
                "vulkanmod-extra.option.happyVillager",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.happyVillager,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.happyVillager = value,
                "vulkanmod-extra.option.happyVillager.tooltip"
            );
            options.add(happyVillagerOption);

            Object angryVillagerOption = createSwitchOption(
                "vulkanmod-extra.option.angryVillager",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.angryVillager,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.angryVillager = value,
                "vulkanmod-extra.option.angryVillager.tooltip"
            );
            options.add(angryVillagerOption);

            Object ashOption = createSwitchOption(
                "vulkanmod-extra.option.ash",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.ash,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.ash = value,
                "vulkanmod-extra.option.ash.tooltip"
            );
            options.add(ashOption);

            Object campfireCosySmokeOption = createSwitchOption(
                "vulkanmod-extra.option.campfireCosySmoke",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.campfireCosySmoke,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.campfireCosySmoke = value,
                "vulkanmod-extra.option.campfireCosySmoke.tooltip"
            );
            options.add(campfireCosySmokeOption);

            Object effectOption = createSwitchOption(
                "vulkanmod-extra.option.effect",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.effect,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.effect = value,
                "vulkanmod-extra.option.effect.tooltip"
            );
            options.add(effectOption);

            Object dustOption = createSwitchOption(
                "vulkanmod-extra.option.dust",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.dust,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.dust = value,
                "vulkanmod-extra.option.dust.tooltip"
            );
            options.add(dustOption);

            Object poofOption = createSwitchOption(
                "vulkanmod-extra.option.poof",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.poof,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.poof = value,
                "vulkanmod-extra.option.poof.tooltip"
            );
            options.add(poofOption);

            Object cherryLeavesOption = createSwitchOption(
                "vulkanmod-extra.option.cherryLeaves",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.cherryLeaves,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.cherryLeaves = value,
                "vulkanmod-extra.option.cherryLeaves.tooltip"
            );
            options.add(cherryLeavesOption);

            Object crimsonSporeOption = createSwitchOption(
                "vulkanmod-extra.option.crimsonSpore",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.crimsonSpore,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.crimsonSpore = value,
                "vulkanmod-extra.option.crimsonSpore.tooltip"
            );
            options.add(crimsonSporeOption);

            Object warpedSporeOption = createSwitchOption(
                "vulkanmod-extra.option.warpedSpore",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.warpedSpore,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.warpedSpore = value,
                "vulkanmod-extra.option.warpedSpore.tooltip"
            );
            options.add(warpedSporeOption);

            Object whiteAshOption = createSwitchOption(
                "vulkanmod-extra.option.whiteAsh",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.whiteAsh,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.whiteAsh = value,
                "vulkanmod-extra.option.whiteAsh.tooltip"
            );
            options.add(whiteAshOption);

            Object sporeBlossomAirOption = createSwitchOption(
                "vulkanmod-extra.option.sporeBlossomAir",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.sporeBlossomAir,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.sporeBlossomAir = value,
                "vulkanmod-extra.option.sporeBlossomAir.tooltip"
            );
            options.add(sporeBlossomAirOption);

            Object myceliumOption = createSwitchOption(
                "vulkanmod-extra.option.mycelium",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.mycelium,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.mycelium = value,
                "vulkanmod-extra.option.mycelium.tooltip"
            );
            options.add(myceliumOption);

            Object cloudOption = createSwitchOption(
                "vulkanmod-extra.option.cloud",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.cloud,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.cloud = value,
                "vulkanmod-extra.option.cloud.tooltip"
            );
            options.add(cloudOption);

            Object composterOption = createSwitchOption(
                "vulkanmod-extra.option.composter",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.composter,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.composter = value,
                "vulkanmod-extra.option.composter.tooltip"
            );
            options.add(composterOption);

            Object drippingHoneyOption = createSwitchOption(
                "vulkanmod-extra.option.drippingHoney",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.drippingHoney,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.drippingHoney = value,
                "vulkanmod-extra.option.drippingHoney.tooltip"
            );
            options.add(drippingHoneyOption);

            Object fallingHoneyOption = createSwitchOption(
                "vulkanmod-extra.option.fallingHoney",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.fallingHoney,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.fallingHoney = value,
                "vulkanmod-extra.option.fallingHoney.tooltip"
            );
            options.add(fallingHoneyOption);

            Object landingHoneyOption = createSwitchOption(
                "vulkanmod-extra.option.landingHoney",
                () -> VulkanModExtraClientConfig.getInstance().particleSettings.landingHoney,
                value -> VulkanModExtraClientConfig.getInstance().particleSettings.landingHoney = value,
                "vulkanmod-extra.option.landingHoney.tooltip"
            );
            options.add(landingHoneyOption);

            Object particleBlock = createOptionBlock("vulkanmod-extra.category.particles", options.toArray());
            return new Object[]{particleBlock};

        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create particle options", e);
            return new Object[0];
        }
    }

    /**
     * Create detail options block
     */
    public static Object[] getDetailOpts() {
        if (!initializeClasses()) {
            return new Object[0];
        }

        try {
            List<Object> options = new ArrayList<>();

            // Sky rendering
            Object skyOption = createSwitchOption(
                "vulkanmod-extra.option.sky",
                () -> VulkanModExtraClientConfig.getInstance().detailSettings.sky,
                value -> VulkanModExtraClientConfig.getInstance().detailSettings.sky = value,
                "vulkanmod-extra.option.sky.tooltip"
            );
            options.add(skyOption);

            // Sun rendering
            Object sunOption = createSwitchOption(
                "vulkanmod-extra.option.sun",
                () -> VulkanModExtraClientConfig.getInstance().detailSettings.sun,
                value -> VulkanModExtraClientConfig.getInstance().detailSettings.sun = value,
                "vulkanmod-extra.option.sun.tooltip"
            );
            options.add(sunOption);

            // Moon rendering
            Object moonOption = createSwitchOption(
                "vulkanmod-extra.option.moon",
                () -> VulkanModExtraClientConfig.getInstance().detailSettings.moon,
                value -> VulkanModExtraClientConfig.getInstance().detailSettings.moon = value,
                "vulkanmod-extra.option.moon.tooltip"
            );
            options.add(moonOption);

            // Stars rendering
            Object starsOption = createSwitchOption(
                "vulkanmod-extra.option.stars",
                () -> VulkanModExtraClientConfig.getInstance().detailSettings.stars,
                value -> VulkanModExtraClientConfig.getInstance().detailSettings.stars = value,
                "vulkanmod-extra.option.stars.tooltip"
            );
            options.add(starsOption);

            // Rain/Snow rendering
            Object rainSnowOption = createSwitchOption(
                "vulkanmod-extra.option.rain_snow",
                () -> VulkanModExtraClientConfig.getInstance().detailSettings.rainSnow,
                value -> VulkanModExtraClientConfig.getInstance().detailSettings.rainSnow = value,
                "vulkanmod-extra.option.rain_snow.tooltip"
            );
            options.add(rainSnowOption);

            // Biome colors
            Object biomeColorsOption = createSwitchOption(
                "vulkanmod-extra.option.biome_colors",
                () -> VulkanModExtraClientConfig.getInstance().detailSettings.biomeColors,
                value -> VulkanModExtraClientConfig.getInstance().detailSettings.biomeColors = value,
                "vulkanmod-extra.option.biome_colors.tooltip"
            );
            options.add(biomeColorsOption);

            // Sky colors
            Object skyColorsOption = createSwitchOption(
                "vulkanmod-extra.option.sky_colors",
                () -> VulkanModExtraClientConfig.getInstance().detailSettings.skyColors,
                value -> VulkanModExtraClientConfig.getInstance().detailSettings.skyColors = value,
                "vulkanmod-extra.option.sky_colors.tooltip"
            );
            options.add(skyColorsOption);

            Object detailBlock = createOptionBlock("vulkanmod-extra.category.details", options.toArray());
            return new Object[]{detailBlock};

        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create detail options", e);
            return new Object[0];
        }
    }

    /**
     * Create render options block
     */
    public static Object[] getRenderOpts() {
        if (!initializeClasses()) {
            return new Object[0];
        }

        try {
            List<Object> options = new ArrayList<>();

            // Global fog
            Object fogOption = createSwitchOption(
                "vulkanmod-extra.option.global_fog",
                () -> VulkanModExtraClientConfig.getInstance().renderSettings.globalFog,
                value -> VulkanModExtraClientConfig.getInstance().renderSettings.globalFog = value,
                "vulkanmod-extra.option.global_fog.tooltip"
            );
            options.add(fogOption);

            // Multi-dimension fog
            Object multiDimFogOption = createSwitchOption(
                "vulkanmod-extra.option.multi_dimension_fog",
                () -> VulkanModExtra.CONFIG.renderSettings.multiDimensionFog,
                value -> VulkanModExtra.CONFIG.renderSettings.multiDimensionFog = value,
                "vulkanmod-extra.option.multi_dimension_fog.tooltip"
            );
            options.add(multiDimFogOption);

            // Light updates
            Object lightUpdatesOption = createSwitchOption(
                "vulkanmod-extra.option.light_updates",
                () -> VulkanModExtraClientConfig.getInstance().renderSettings.lightUpdates,
                value -> VulkanModExtraClientConfig.getInstance().renderSettings.lightUpdates = value,
                "vulkanmod-extra.option.light_updates.tooltip"
            );
            options.add(lightUpdatesOption);

            // Item frame rendering
            Object itemFrameOption = createSwitchOption(
                "vulkanmod-extra.option.item_frame",
                () -> VulkanModExtraClientConfig.getInstance().renderSettings.itemFrame,
                value -> VulkanModExtraClientConfig.getInstance().renderSettings.itemFrame = value,
                "vulkanmod-extra.option.item_frame.tooltip"
            );
            options.add(itemFrameOption);

            // Armor stand rendering
            Object armorStandOption = createSwitchOption(
                "vulkanmod-extra.option.armor_stand",
                () -> VulkanModExtraClientConfig.getInstance().renderSettings.armorStand,
                value -> VulkanModExtraClientConfig.getInstance().renderSettings.armorStand = value,
                "vulkanmod-extra.option.armor_stand.tooltip"
            );
            options.add(armorStandOption);

            // Painting rendering
            Object paintingOption = createSwitchOption(
                "vulkanmod-extra.option.painting",
                () -> VulkanModExtraClientConfig.getInstance().renderSettings.painting,
                value -> VulkanModExtraClientConfig.getInstance().renderSettings.painting = value,
                "vulkanmod-extra.option.painting.tooltip"
            );
            options.add(paintingOption);

            // Piston rendering
            Object pistonOption = createSwitchOption(
                "vulkanmod-extra.option.piston",
                () -> VulkanModExtraClientConfig.getInstance().renderSettings.piston,
                value -> VulkanModExtraClientConfig.getInstance().renderSettings.piston = value,
                "vulkanmod-extra.option.piston.tooltip"
            );
            options.add(pistonOption);

            // Beacon beam rendering
            Object beaconBeamOption = createSwitchOption(
                "vulkanmod-extra.option.beacon_beam",
                () -> VulkanModExtraClientConfig.getInstance().renderSettings.beaconBeam,
                value -> VulkanModExtraClientConfig.getInstance().renderSettings.beaconBeam = value,
                "vulkanmod-extra.option.beacon_beam.tooltip"
            );
            options.add(beaconBeamOption);

            // Limit beacon beam height
            Object limitBeaconBeamOption = createSwitchOption(
                "vulkanmod-extra.option.limit_beacon_beam_height",
                () -> VulkanModExtra.CONFIG.renderSettings.limitBeaconBeamHeight,
                value -> VulkanModExtra.CONFIG.renderSettings.limitBeaconBeamHeight = value,
                "vulkanmod-extra.option.limit_beacon_beam_height.tooltip"
            );
            options.add(limitBeaconBeamOption);

            // Enchanting table book animation
            Object enchantingBookOption = createSwitchOption(
                "vulkanmod-extra.option.enchanting_table_book",
                () -> VulkanModExtraClientConfig.getInstance().renderSettings.enchantingTableBook,
                value -> VulkanModExtraClientConfig.getInstance().renderSettings.enchantingTableBook = value,
                "vulkanmod-extra.option.enchanting_table_book.tooltip"
            );
            options.add(enchantingBookOption);

            // Item frame name tags
            Object itemFrameNameTagOption = createSwitchOption(
                "vulkanmod-extra.option.item_frame_name_tag",
                () -> VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag,
                value -> VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag = value,
                "vulkanmod-extra.option.item_frame_name_tag.tooltip"
            );
            options.add(itemFrameNameTagOption);

            // Player name tags
            Object playerNameTagOption = createSwitchOption(
                "vulkanmod-extra.option.player_name_tag",
                () -> VulkanModExtra.CONFIG.renderSettings.playerNameTag,
                value -> VulkanModExtra.CONFIG.renderSettings.playerNameTag = value,
                "vulkanmod-extra.option.player_name_tag.tooltip"
            );
            options.add(playerNameTagOption);

            // Create first block for basic render options
            Object renderBlock = createOptionBlock("vulkanmod-extra.category.render", options.toArray());

            // Create separate blocks for fog configuration per fog type
            List<Object[]> fogBlocks = new ArrayList<>();
            fogBlocks.add(new Object[]{renderBlock});

            // Add fog type configuration blocks
            List<Object> fogOptions = new ArrayList<>();

            // Water fog
            Object waterFogOption = createSwitchOption(
                "vulkanmod-extra.option.fog_type.water",
                () -> getFogTypeEnabled(FogType.WATER),
                value -> setFogTypeEnabled(FogType.WATER, value),
                "vulkanmod-extra.option.fog_type.water.tooltip"
            );
            fogOptions.add(waterFogOption);

            // Lava fog
            Object lavaFogOption = createSwitchOption(
                "vulkanmod-extra.option.fog_type.lava",
                () -> getFogTypeEnabled(FogType.LAVA),
                value -> setFogTypeEnabled(FogType.LAVA, value),
                "vulkanmod-extra.option.fog_type.lava.tooltip"
            );
            fogOptions.add(lavaFogOption);

            // Powder snow fog
            Object powderSnowFogOption = createSwitchOption(
                "vulkanmod-extra.option.fog_type.powder_snow",
                () -> getFogTypeEnabled(FogType.POWDER_SNOW),
                value -> setFogTypeEnabled(FogType.POWDER_SNOW, value),
                "vulkanmod-extra.option.fog_type.powder_snow.tooltip"
            );
            fogOptions.add(powderSnowFogOption);

            // Create fog configuration block
            fogBlocks.add(new Object[]{createOptionBlock("vulkanmod-extra.category.fog_types", fogOptions.toArray())});

            // Add detailed fog multiplier controls for each fog type
            addFogMultiplierControls(fogBlocks);

            return fogBlocks.toArray(new Object[0]);

        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create render options", e);
            return new Object[0];
        }
    }

    /**
     * Create extra options blocks
     */
    public static Object[] getExtraOpts() {
        if (!initializeClasses()) {
            return new Object[0];
        }

        try {
            List<Object[]> blocks = new ArrayList<>();

            // HUD and overlay settings
            List<Object> hudOptions = new ArrayList<>();

            // Overlay corner option
            Object[] overlayCorners = getOverlayCornerValues();
            Object overlayCornerOption = createCyclingOption(
                "vulkanmod-extra.option.overlay_corner",
                overlayCorners,
                () -> VulkanModExtra.CONFIG.hudSettings.overlayCorner,
                value -> VulkanModExtra.CONFIG.hudSettings.overlayCorner = (com.criticalrange.config.VulkanModExtraConfig.OverlayCorner) value,
                corner -> corner.toString().toLowerCase().replace("_", " "),
                "vulkanmod-extra.option.overlay_corner.tooltip"
            );
            hudOptions.add(overlayCornerOption);

            // Text contrast option
            Object[] textContrasts = getTextContrastValues();
            Object textContrastOption = createCyclingOption(
                "vulkanmod-extra.option.text_contrast",
                textContrasts,
                () -> VulkanModExtra.CONFIG.hudSettings.textContrast,
                value -> VulkanModExtra.CONFIG.hudSettings.textContrast = (com.criticalrange.config.VulkanModExtraConfig.TextContrast) value,
                contrast -> contrast.toString().toLowerCase(),
                "vulkanmod-extra.option.text_contrast.tooltip"
            );
            hudOptions.add(textContrastOption);

            Object fpsOption = createSwitchOption(
                "vulkanmod-extra.option.show_fps",
                () -> VulkanModExtra.CONFIG.hudSettings.showFps,
                value -> VulkanModExtra.CONFIG.hudSettings.showFps = value,
                "vulkanmod-extra.option.show_fps.tooltip"
            );
            hudOptions.add(fpsOption);

            Object fpsExtendedOption = createSwitchOption(
                "vulkanmod-extra.option.show_fps_extended",
                () -> VulkanModExtra.CONFIG.hudSettings.showFPSExtended,
                value -> VulkanModExtra.CONFIG.hudSettings.showFPSExtended = value,
                "vulkanmod-extra.option.show_fps_extended.tooltip"
            );
            hudOptions.add(fpsExtendedOption);

            Object coordsOption = createSwitchOption(
                "vulkanmod-extra.option.show_coords",
                () -> VulkanModExtra.CONFIG.hudSettings.showCoords,
                value -> VulkanModExtra.CONFIG.hudSettings.showCoords = value,
                "vulkanmod-extra.option.show_coords.tooltip"
            );
            hudOptions.add(coordsOption);

            // Performance and advanced options
            Object reduceResolutionOption = createSwitchOption(
                "vulkanmod-extra.option.reduce_resolution_on_mac",
                () -> VulkanModExtra.CONFIG.performanceSettings.reduceResolutionOnMac,
                value -> VulkanModExtra.CONFIG.performanceSettings.reduceResolutionOnMac = value,
                "vulkanmod-extra.option.reduce_resolution_on_mac.tooltip"
            );
            hudOptions.add(reduceResolutionOption);

            Object adaptiveSyncOption = createSwitchOption(
                "vulkanmod-extra.option.use_adaptive_sync",
                () -> VulkanModExtra.CONFIG.performanceSettings.useAdaptiveSync,
                value -> VulkanModExtra.CONFIG.performanceSettings.useAdaptiveSync = value,
                "vulkanmod-extra.option.use_adaptive_sync.tooltip"
            );
            hudOptions.add(adaptiveSyncOption);

            Object instantSneakOption = createSwitchOption(
                "vulkanmod-extra.option.instant_sneak",
                () -> VulkanModExtra.CONFIG.performanceSettings.instantSneak,
                value -> VulkanModExtra.CONFIG.performanceSettings.instantSneak = value,
                "vulkanmod-extra.option.instant_sneak.tooltip"
            );
            hudOptions.add(instantSneakOption);

            Object preventShadersOption = createSwitchOption(
                "vulkanmod-extra.option.prevent_shaders",
                () -> VulkanModExtra.CONFIG.performanceSettings.preventShaders,
                value -> VulkanModExtra.CONFIG.performanceSettings.preventShaders = value,
                "vulkanmod-extra.option.prevent_shaders.tooltip"
            );
            hudOptions.add(preventShadersOption);

            Object steadyDebugHudOption = createSwitchOption(
                "vulkanmod-extra.option.steady_debug_hud",
                () -> VulkanModExtra.CONFIG.performanceSettings.steadyDebugHud,
                value -> VulkanModExtra.CONFIG.performanceSettings.steadyDebugHud = value,
                "vulkanmod-extra.option.steady_debug_hud.tooltip"
            );
            hudOptions.add(steadyDebugHudOption);

            // Create first block for HUD and performance options
            blocks.add(new Object[]{createOptionBlock("vulkanmod-extra.category.extra", hudOptions.toArray())});

            // Advanced features block
            List<Object> advancedOptions = new ArrayList<>();

            Object advancedItemTooltipsOption = createSwitchOption(
                "vulkanmod-extra.option.advanced_item_tooltips",
                () -> VulkanModExtra.CONFIG.performanceSettings.advancedItemTooltips,
                value -> VulkanModExtra.CONFIG.performanceSettings.advancedItemTooltips = value,
                "vulkanmod-extra.option.advanced_item_tooltips.tooltip"
            );
            advancedOptions.add(advancedItemTooltipsOption);

            Object fastRandomOption = createSwitchOption(
                "vulkanmod-extra.option.use_fast_random",
                () -> VulkanModExtra.CONFIG.performanceSettings.useFastRandom,
                value -> VulkanModExtra.CONFIG.performanceSettings.useFastRandom = value,
                "vulkanmod-extra.option.use_fast_random.tooltip"
            );
            advancedOptions.add(fastRandomOption);

            Object linearColorBlenderOption = createSwitchOption(
                "vulkanmod-extra.option.linear_flat_color_blender",
                () -> VulkanModExtra.CONFIG.performanceSettings.linearFlatColorBlender,
                value -> VulkanModExtra.CONFIG.performanceSettings.linearFlatColorBlender = value,
                "vulkanmod-extra.option.linear_flat_color_blender.tooltip"
            );
            advancedOptions.add(linearColorBlenderOption);

            // Cloud settings
            // Note: These would need range options for sliders, but using switch for now
            Object cloudHeightOption = createSwitchOption(
                "vulkanmod-extra.option.cloud_height",
                () -> VulkanModExtra.CONFIG.environmentSettings.cloudHeight > 0,
                value -> VulkanModExtra.CONFIG.environmentSettings.cloudHeight = value ? 192 : 0,
                "vulkanmod-extra.option.cloud_height.tooltip"
            );
            advancedOptions.add(cloudHeightOption);

            Object cloudDistanceOption = createSwitchOption(
                "vulkanmod-extra.option.cloud_distance",
                () -> VulkanModExtra.CONFIG.environmentSettings.cloudDistance > 0,
                value -> VulkanModExtra.CONFIG.environmentSettings.cloudDistance = value ? 100 : 0,
                "vulkanmod-extra.option.cloud_distance.tooltip"
            );
            advancedOptions.add(cloudDistanceOption);

            blocks.add(new Object[]{createOptionBlock("vulkanmod-extra.category.extra", advancedOptions.toArray())});

            // Toast notifications block
            List<Object> toastOptions = new ArrayList<>();

            Object toastsOption = createSwitchOption(
                "vulkanmod-extra.option.toasts",
                () -> VulkanModExtra.CONFIG.hudSettings.toasts,
                value -> VulkanModExtra.CONFIG.hudSettings.toasts = value,
                "vulkanmod-extra.option.toasts.tooltip"
            );
            toastOptions.add(toastsOption);

            Object advancementToastOption = createSwitchOption(
                "vulkanmod-extra.option.advancement_toast",
                () -> VulkanModExtra.CONFIG.hudSettings.advancementToast,
                value -> VulkanModExtra.CONFIG.hudSettings.advancementToast = value,
                "vulkanmod-extra.option.advancement_toast.tooltip"
            );
            toastOptions.add(advancementToastOption);

            Object recipeToastOption = createSwitchOption(
                "vulkanmod-extra.option.recipe_toast",
                () -> VulkanModExtra.CONFIG.hudSettings.recipeToast,
                value -> VulkanModExtra.CONFIG.hudSettings.recipeToast = value,
                "vulkanmod-extra.option.recipe_toast.tooltip"
            );
            toastOptions.add(recipeToastOption);

            Object systemToastOption = createSwitchOption(
                "vulkanmod-extra.option.system_toast",
                () -> VulkanModExtra.CONFIG.hudSettings.systemToast,
                value -> VulkanModExtra.CONFIG.hudSettings.systemToast = value,
                "vulkanmod-extra.option.system_toast.tooltip"
            );
            toastOptions.add(systemToastOption);

            Object tutorialToastOption = createSwitchOption(
                "vulkanmod-extra.option.tutorial_toast",
                () -> VulkanModExtra.CONFIG.hudSettings.tutorialToast,
                value -> VulkanModExtra.CONFIG.hudSettings.tutorialToast = value,
                "vulkanmod-extra.option.tutorial_toast.tooltip"
            );
            toastOptions.add(tutorialToastOption);

            blocks.add(new Object[]{createOptionBlock("vulkanmod-extra.category.extra", toastOptions.toArray())});

            return blocks.toArray(new Object[0]);

        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create extra options", e);
            return new Object[0];
        }
    }

    // Helper methods for creating VulkanMod option objects via reflection

    private static Object createSwitchOption(String translationKey, java.util.function.BooleanSupplier getter,
                                           java.util.function.Consumer<Boolean> setter, String tooltipKey) throws Exception {
        Constructor<?> constructor = switchOptionClass.getConstructor(componentClass, java.util.function.BooleanSupplier.class, java.util.function.Consumer.class);
        Object component = createTranslatableComponent(translationKey);

        Object option = constructor.newInstance(component, getter, setter);

        // Set tooltip if available
        if (tooltipKey != null) {
            Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", componentClass);
            Object tooltipComponent = createTranslatableComponent(tooltipKey);
            setTooltipMethod.invoke(option, tooltipComponent);
        }

        return option;
    }

    private static Object createCyclingOption(String translationKey, Object[] values,
                                            java.util.function.Supplier<?> getter,
                                            java.util.function.Consumer<?> setter,
                                            java.util.function.Function<?, String> translator,
                                            String tooltipKey) throws Exception {
        Constructor<?> constructor = cyclingOptionClass.getConstructor(componentClass, Object[].class,
                java.util.function.Supplier.class, java.util.function.Consumer.class);
        Object component = createTranslatableComponent(translationKey);

        Object option = constructor.newInstance(component, values, getter, setter);

        // Set translator
        Method setTranslatorMethod = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
        setTranslatorMethod.invoke(option, translator);

        // Set tooltip if available
        if (tooltipKey != null) {
            Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", componentClass);
            Object tooltipComponent = createTranslatableComponent(tooltipKey);
            setTooltipMethod.invoke(option, tooltipComponent);
        }

        return option;
    }

    private static Object createRangeOption(String translationKey, int min, int max, int step,
                                           java.util.function.IntSupplier getter,
                                           java.util.function.IntConsumer setter,
                                           String tooltipKey) throws Exception {
        Constructor<?> constructor = rangeOptionClass.getConstructor(
            componentClass, int.class, int.class, int.class,
            java.util.function.IntSupplier.class, java.util.function.IntConsumer.class
        );
        Object component = createTranslatableComponent(translationKey);

        Object option = constructor.newInstance(component, min, max, step, getter, setter);

        // Set tooltip if available
        if (tooltipKey != null) {
            Method setTooltipMethod = rangeOptionClass.getMethod("setTooltip", componentClass);
            Object tooltipComponent = createTranslatableComponent(tooltipKey);
            setTooltipMethod.invoke(option, tooltipComponent);
        }

        return option;
    }

    private static Object createOptionBlock(String titleKey, Object[] options) throws Exception {
        Constructor<?> constructor = optionBlockClass.getConstructor(componentClass, Object[].class);
        Object titleComponent = createTranslatableComponent(titleKey);
        return constructor.newInstance(titleComponent, options);
    }

    private static Object createTranslatableComponent(String key) throws Exception {
        Method translatableMethod = componentClass.getMethod("translatable", String.class);
        return translatableMethod.invoke(null, key);
    }

    /**
     * Get fog type enabled state
     */
    private static boolean getFogTypeEnabled(FogType fogType) {
        try {
            var config = VulkanModExtra.CONFIG.renderSettings.fogTypeConfig.get(fogType);
            return config != null ? config.enable : true;
        } catch (Exception e) {
            return true; // Default to enabled
        }
    }

    /**
     * Set fog type enabled state
     */
    private static void setFogTypeEnabled(FogType fogType, boolean enabled) {
        try {
            var config = VulkanModExtra.CONFIG.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new com.criticalrange.config.VulkanModExtraConfig.FogTypeConfig());
            config.enable = enabled;
            VulkanModExtra.CONFIG.writeChanges();
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to set fog type enabled state", e);
        }
    }

    /**
     * Get overlay corner enum values
     */
    private static Object[] getOverlayCornerValues() {
        try {
            Class<?> configClass = Class.forName("com.criticalrange.config.VulkanModExtraConfig");
            Class<?>[] nestedClasses = configClass.getDeclaredClasses();
            for (Class<?> nestedClass : nestedClasses) {
                if (nestedClass.getSimpleName().equals("OverlayCorner")) {
                    return nestedClass.getEnumConstants();
                }
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to get overlay corner values", e);
        }
        // Fallback
        return new Object[]{"TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT"};
    }

    /**
     * Get text contrast enum values
     */
    private static Object[] getTextContrastValues() {
        try {
            Class<?> configClass = Class.forName("com.criticalrange.config.VulkanModExtraConfig");
            Class<?>[] nestedClasses = configClass.getDeclaredClasses();
            for (Class<?> nestedClass : nestedClasses) {
                if (nestedClass.getSimpleName().equals("TextContrast")) {
                    return nestedClass.getEnumConstants();
                }
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to get text contrast values", e);
        }
        // Fallback
        return new Object[]{"NONE", "BACKGROUND", "SHADOW"};
    }

    /**
     * Add detailed fog multiplier controls for each fog type
     */
    private static void addFogMultiplierControls(List<Object[]> fogBlocks) {
        if (!initializeClasses()) {
            return;
        }

        try {
            FogType[] fogTypes = {FogType.WATER, FogType.LAVA, FogType.POWDER_SNOW};

            for (FogType fogType : fogTypes) {
                List<Object> multiplierOptions = new ArrayList<>();

                String fogTypeName = getFogTypeDisplayName(fogType);

                // Environment Start Multiplier
                Object envStartOption = createRangeOption(
                    String.format("vulkanmod-extra.option.fog_type.environment_start", fogTypeName),
                    0, 500, 1, // min, max, step
                    () -> getFogTypeMultiplier(fogType, "environmentStart"),
                    value -> setFogTypeMultiplier(fogType, "environmentStart", value),
                    String.format("vulkanmod-extra.option.fog_type.environment_start.tooltip", fogTypeName)
                );
                multiplierOptions.add(envStartOption);

                // Environment End Multiplier
                Object envEndOption = createRangeOption(
                    String.format("vulkanmod-extra.option.fog_type.environment_end", fogTypeName),
                    0, 500, 1,
                    () -> getFogTypeMultiplier(fogType, "environmentEnd"),
                    value -> setFogTypeMultiplier(fogType, "environmentEnd", value),
                    String.format("vulkanmod-extra.option.fog_type.environment_end.tooltip", fogTypeName)
                );
                multiplierOptions.add(envEndOption);

                // Render Distance Start Multiplier
                Object renderStartOption = createRangeOption(
                    String.format("vulkanmod-extra.option.fog_type.render_distance_start", fogTypeName),
                    0, 500, 1,
                    () -> getFogTypeMultiplier(fogType, "renderDistanceStart"),
                    value -> setFogTypeMultiplier(fogType, "renderDistanceStart", value),
                    String.format("vulkanmod-extra.option.fog_type.render_distance_start.tooltip", fogTypeName)
                );
                multiplierOptions.add(renderStartOption);

                // Render Distance End Multiplier
                Object renderEndOption = createRangeOption(
                    String.format("vulkanmod-extra.option.fog_type.render_distance_end", fogTypeName),
                    0, 500, 1,
                    () -> getFogTypeMultiplier(fogType, "renderDistanceEnd"),
                    value -> setFogTypeMultiplier(fogType, "renderDistanceEnd", value),
                    String.format("vulkanmod-extra.option.fog_type.render_distance_end.tooltip", fogTypeName)
                );
                multiplierOptions.add(renderEndOption);

                // Sky End Multiplier
                Object skyEndOption = createRangeOption(
                    String.format("vulkanmod-extra.option.fog_type.sky_end", fogTypeName),
                    0, 500, 1,
                    () -> getFogTypeMultiplier(fogType, "skyEnd"),
                    value -> setFogTypeMultiplier(fogType, "skyEnd", value),
                    String.format("vulkanmod-extra.option.fog_type.sky_end.tooltip", fogTypeName)
                );
                multiplierOptions.add(skyEndOption);

                // Cloud End Multiplier
                Object cloudEndOption = createRangeOption(
                    String.format("vulkanmod-extra.option.fog_type.cloud_end", fogTypeName),
                    0, 500, 1,
                    () -> getFogTypeMultiplier(fogType, "cloudEnd"),
                    value -> setFogTypeMultiplier(fogType, "cloudEnd", value),
                    String.format("vulkanmod-extra.option.fog_type.cloud_end.tooltip", fogTypeName)
                );
                multiplierOptions.add(cloudEndOption);

                fogBlocks.add(new Object[]{createOptionBlock(
                    String.format("vulkanmod-extra.category.fog_multipliers.%s", fogTypeName.toLowerCase()),
                    multiplierOptions.toArray()
                )});
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create fog multiplier controls", e);
        }
    }

    /**
     * Get display name for fog type
     */
    private static String getFogTypeDisplayName(FogType fogType) {
        return switch (fogType) {
            case WATER -> "Water";
            case LAVA -> "Lava";
            case POWDER_SNOW -> "Powder Snow";
            default -> fogType.name();
        };
    }

    /**
     * Get fog type multiplier value
     */
    private static int getFogTypeMultiplier(FogType fogType, String multiplierType) {
        try {
            var config = VulkanModExtra.CONFIG.renderSettings.fogTypeConfig.get(fogType);
            if (config != null) {
                return switch (multiplierType) {
                    case "environmentStart" -> config.environmentStartMultiplier;
                    case "environmentEnd" -> config.environmentEndMultiplier;
                    case "renderDistanceStart" -> config.renderDistanceStartMultiplier;
                    case "renderDistanceEnd" -> config.renderDistanceEndMultiplier;
                    case "skyEnd" -> config.skyEndMultiplier;
                    case "cloudEnd" -> config.cloudEndMultiplier;
                    default -> 100;
                };
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to get fog type multiplier", e);
        }
        return 100; // Default value
    }

    /**
     * Set fog type multiplier value
     */
    private static void setFogTypeMultiplier(FogType fogType, String multiplierType, int value) {
        try {
            var config = VulkanModExtra.CONFIG.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new com.criticalrange.config.VulkanModExtraConfig.FogTypeConfig());
            switch (multiplierType) {
                case "environmentStart" -> config.environmentStartMultiplier = value;
                case "environmentEnd" -> config.environmentEndMultiplier = value;
                case "renderDistanceStart" -> config.renderDistanceStartMultiplier = value;
                case "renderDistanceEnd" -> config.renderDistanceEndMultiplier = value;
                case "skyEnd" -> config.skyEndMultiplier = value;
                case "cloudEnd" -> config.cloudEndMultiplier = value;
            }
            VulkanModExtra.CONFIG.writeChanges();
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to set fog type multiplier", e);
        }
    }
}
