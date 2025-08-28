package com.criticalrange.client;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.client.config.VulkanModExtraClientConfig;

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

            // Enchanting table book animation
            Object enchantingBookOption = createSwitchOption(
                "vulkanmod-extra.option.enchanting_table_book",
                () -> VulkanModExtraClientConfig.getInstance().renderSettings.enchantingTableBook,
                value -> VulkanModExtraClientConfig.getInstance().renderSettings.enchantingTableBook = value,
                "vulkanmod-extra.option.enchanting_table_book.tooltip"
            );
            options.add(enchantingBookOption);

            Object renderBlock = createOptionBlock("vulkanmod-extra.category.render", options.toArray());
            return new Object[]{renderBlock};

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
                () -> VulkanModExtra.CONFIG.extraSettings.overlayCorner,
                value -> VulkanModExtra.CONFIG.extraSettings.overlayCorner = (com.criticalrange.config.VulkanModExtraConfig.OverlayCorner) value,
                corner -> corner.toString().toLowerCase().replace("_", " "),
                "vulkanmod-extra.option.overlay_corner.tooltip"
            );
            hudOptions.add(overlayCornerOption);

            // Text contrast option
            Object[] textContrasts = getTextContrastValues();
            Object textContrastOption = createCyclingOption(
                "vulkanmod-extra.option.text_contrast",
                textContrasts,
                () -> VulkanModExtra.CONFIG.extraSettings.textContrast,
                value -> VulkanModExtra.CONFIG.extraSettings.textContrast = (com.criticalrange.config.VulkanModExtraConfig.TextContrast) value,
                contrast -> contrast.toString().toLowerCase(),
                "vulkanmod-extra.option.text_contrast.tooltip"
            );
            hudOptions.add(textContrastOption);

            Object fpsOption = createSwitchOption(
                "vulkanmod-extra.option.show_fps",
                () -> VulkanModExtra.CONFIG.extraSettings.showFps,
                value -> VulkanModExtra.CONFIG.extraSettings.showFps = value,
                "vulkanmod-extra.option.show_fps.tooltip"
            );
            hudOptions.add(fpsOption);

            Object fpsExtendedOption = createSwitchOption(
                "vulkanmod-extra.option.show_fps_extended",
                () -> VulkanModExtra.CONFIG.extraSettings.showFPSExtended,
                value -> VulkanModExtra.CONFIG.extraSettings.showFPSExtended = value,
                "vulkanmod-extra.option.show_fps_extended.tooltip"
            );
            hudOptions.add(fpsExtendedOption);

            Object coordsOption = createSwitchOption(
                "vulkanmod-extra.option.show_coords",
                () -> VulkanModExtra.CONFIG.extraSettings.showCoords,
                value -> VulkanModExtra.CONFIG.extraSettings.showCoords = value,
                "vulkanmod-extra.option.show_coords.tooltip"
            );
            hudOptions.add(coordsOption);

            blocks.add(new Object[]{createOptionBlock("vulkanmod-extra.category.extra", hudOptions.toArray())});

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
}
