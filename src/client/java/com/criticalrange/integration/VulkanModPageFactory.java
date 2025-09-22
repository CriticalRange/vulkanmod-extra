package com.criticalrange.integration;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.config.VulkanModExtraConfig;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Factory for creating VulkanMod-compatible option pages.
 * Creates full option pages with actual settings controls.
 */
public class VulkanModPageFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Page Factory");

    /**
     * Get boolean field value using reflection
     */
    private static boolean getBooleanField(Object target, String fieldName) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getBoolean(target);
        } catch (Exception e) {
            return true; // Default value
        }
    }

    /**
     * Set boolean field value using reflection
     */
    private static void setBooleanField(Object target, String fieldName, boolean value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(target, value);
            saveConfig();
        } catch (Exception e) {
            LOGGER.warn("Failed to set field: " + fieldName, e);
        }
    }

    // Cache VulkanMod classes
    private static Class<?> cachedOptionPageClass;
    private static Class<?> cachedOptionBlockClass;
    private static Class<?> cachedSwitchOptionClass;
    private static Class<?> cachedCyclingOptionClass;
    private static Class<?> cachedOptionClass;
    private static boolean classesLoaded = false;

    /**
     * Create VulkanMod Extra option pages for the event-based integration system
     */
    public static List<Object> createOptionPages() {
        List<Object> pages = new ArrayList<>();

        try {
            if (!loadVulkanModClasses()) {
                LOGGER.warn("Cannot create VulkanMod Extra pages - VulkanMod classes not available");
                return pages;
            }

            if (VulkanModExtra.CONFIG != null) {
                // Create pages for each major category with actual options
                pages.add(createAnimationsPage());
                pages.add(createParticlesPage());
                pages.add(createDetailsPage());
                pages.add(createRenderPage());
                pages.add(createExtraPage());

                // Only add optimization page if memory pooling is supported
                if (com.criticalrange.util.VulkanModVersionHelper.isMemoryPoolingSupported()) {
                    pages.add(createOptimizationPage());
                    LOGGER.info("Added Optimization page with memory pooling support");
                } else {
                    LOGGER.info("Skipped Optimization page - memory pooling not supported in VulkanMod {}",
                        com.criticalrange.util.VulkanModVersionHelper.getVulkanModVersion());
                }

                LOGGER.info("Created {} VulkanMod Extra option pages with settings", pages.size());
            }

        } catch (Exception e) {
            LOGGER.warn("Error creating VulkanMod Extra option pages", e);
        }

        return pages;
    }

    private static boolean loadVulkanModClasses() {
        if (classesLoaded) return true;
        try {
            cachedOptionPageClass = Class.forName("net.vulkanmod.config.option.OptionPage");
            cachedOptionBlockClass = Class.forName("net.vulkanmod.config.gui.OptionBlock");
            cachedSwitchOptionClass = Class.forName("net.vulkanmod.config.option.SwitchOption");
            cachedCyclingOptionClass = Class.forName("net.vulkanmod.config.option.CyclingOption");
            cachedOptionClass = Class.forName("net.vulkanmod.config.option.Option");
            classesLoaded = true;
            return true;
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed to load VulkanMod classes for GUI integration", e);
            return false;
        }
    }

    private static Object createAnimationsPage() throws Exception {
        return createPageWithBlocks("Animations", createAnimationOptionBlocks());
    }

    private static Object[] createAnimationOptionBlocks() throws Exception {
        // Create master toggle block
        List<Object> masterOptions = new ArrayList<>();
        masterOptions.add(createSwitchOption(
            "All Animations",
            "vulkanmod-extra.option.animation.allAnimations.tooltip",
            () -> VulkanModExtra.CONFIG.animationSettings.allAnimations,
            (value) -> {
                VulkanModExtra.CONFIG.animationSettings.allAnimations = value;
                saveConfig();
            }
        ));

        // Create individual animation options
        List<Object> individualOptions = new ArrayList<>();

        // Fluid animations
        String[] fluidAnimations = {"water", "waterStill", "waterFlow", "lava", "lavaStill", "lavaFlow"};
        for (String type : fluidAnimations) {
            individualOptions.add(createAnimationOption(type));
        }

        // Fire & light animations
        String[] fireAnimations = {"fire", "fire0", "fire1", "soulFire", "soulFire0", "soulFire1",
                                  "campfireFire", "soulCampfireFire", "lantern", "soulLantern", "seaLantern"};
        for (String type : fireAnimations) {
            individualOptions.add(createAnimationOption(type));
        }

        // Portal animations
        String[] portalAnimations = {"portal", "netherPortal", "endPortal", "endGateway"};
        for (String type : portalAnimations) {
            individualOptions.add(createAnimationOption(type));
        }

        // Block animations
        String[] blockAnimations = {"blockAnimations", "magma", "prismarine", "prismarineBricks",
                                   "darkPrismarine", "conduit", "respawnAnchor", "stonecutterSaw"};
        for (String type : blockAnimations) {
            individualOptions.add(createAnimationOption(type));
        }

        // Machine animations
        String[] machineAnimations = {"machineAnimations", "blastFurnaceFrontOn", "smokerFrontOn", "furnaceFrontOn"};
        for (String type : machineAnimations) {
            individualOptions.add(createAnimationOption(type));
        }

        // Plant animations
        String[] plantAnimations = {"plantAnimations", "kelp", "kelpPlant", "seagrass", "tallSeagrassBottom", "tallSeagrassTop"};
        for (String type : plantAnimations) {
            individualOptions.add(createAnimationOption(type));
        }

        // Stem animations
        String[] stemAnimations = {"stemAnimations", "warpedStem", "crimsonStem", "warpedHyphae", "crimsonHyphae"};
        for (String type : stemAnimations) {
            individualOptions.add(createAnimationOption(type));
        }

        // Sculk animations
        String[] sculkAnimations = {"sculkAnimations", "sculk", "sculkVein", "sculkSensor", "sculkSensorSide",
                                   "sculkSensorTop", "sculkShrieker", "sculkShriekerSide", "sculkShriekerTop",
                                   "calibratedSculkSensor", "calibratedSculkSensorSide", "calibratedSculkSensorTop"};
        for (String type : sculkAnimations) {
            individualOptions.add(createAnimationOption(type));
        }

        // Command block animations
        String[] commandBlockAnimations = {"commandBlockAnimations", "commandBlockFront", "chainCommandBlockFront", "repeatingCommandBlockFront"};
        for (String type : commandBlockAnimations) {
            individualOptions.add(createAnimationOption(type));
        }

        // Additional animations
        String[] additionalAnimations = {"additionalAnimations", "beacon", "dragonEgg", "brewingStandBase", "cauldronWater", "enchantingTableBook"};
        for (String type : additionalAnimations) {
            individualOptions.add(createAnimationOption(type));
        }

        // Create blocks
        Object masterBlock = createBlock("Master Controls", masterOptions);
        Object individualBlock = createBlock("Individual Controls", individualOptions);

        return new Object[]{masterBlock, individualBlock};
    }

    private static Object createAnimationOption(String fieldName) throws Exception {
        return createSwitchOption(
            Text.translatable("vulkanmod-extra.option.animation." + fieldName),
            "vulkanmod-extra.option.animation." + fieldName + ".tooltip",
            () -> getBooleanField(VulkanModExtra.CONFIG.animationSettings, fieldName),
            (value) -> setBooleanField(VulkanModExtra.CONFIG.animationSettings, fieldName, value)
        );
    }


    private static Object createParticlesPage() throws Exception {
        return createPageWithBlocks("Particles", createParticleOptionBlocks());
    }

    private static Object[] createParticleOptionBlocks() throws Exception {
        // Create master toggle block
        List<Object> masterOptions = new ArrayList<>();
        masterOptions.add(createSwitchOption(
            "All Particles",
            "vulkanmod-extra.option.particle.allParticles.tooltip",
            () -> VulkanModExtra.CONFIG.particleSettings.allParticles,
            (value) -> {
                VulkanModExtra.CONFIG.particleSettings.allParticles = value;
                saveConfig();
            }
        ));

        // Create individual particle options using all available fields
        List<Object> individualOptions = new ArrayList<>();

        // Get all particle field names automatically using reflection
        try {
            java.lang.reflect.Field[] fields = VulkanModExtra.CONFIG.particleSettings.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                if (field.getType() == boolean.class && !field.getName().equals("allParticles")) {
                    individualOptions.add(createParticleOption(field.getName()));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to create particle options via reflection", e);
            // Fallback to manual list of common particles
            String[] commonParticles = {
                "rainSplash", "blockBreak", "blockBreaking", "flame", "smoke", "bubble", "drippingWater",
                "explosion", "heart", "crit", "enchant", "portal", "lava", "firework", "happyVillager",
                "angryVillager", "ash", "campfireCosySmoke", "effect", "dust", "largeSmoke", "endRod"
            };
            for (String particle : commonParticles) {
                individualOptions.add(createParticleOption(particle));
            }
        }

        // Create blocks
        Object masterBlock = createBlock("Master Controls", masterOptions);
        Object individualBlock = createBlock("Individual Controls", individualOptions);

        return new Object[]{masterBlock, individualBlock};
    }

    private static Object createParticleOption(String fieldName) throws Exception {
        return createSwitchOption(
            Text.translatable("vulkanmod-extra.option.particle." + fieldName),
            "vulkanmod-extra.option.particle." + fieldName + ".tooltip",
            () -> getBooleanField(VulkanModExtra.CONFIG.particleSettings, fieldName),
            (value) -> setBooleanField(VulkanModExtra.CONFIG.particleSettings, fieldName, value)
        );
    }


    private static Object createDetailsPage() throws Exception {
        List<Object> options = new ArrayList<>();

        // Sky and celestial elements
        options.add(createSwitchOption(
            "Sky Rendering",
            "vulkanmod-extra.option.details.sky.tooltip",
            () -> VulkanModExtra.CONFIG.detailSettings.sky,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.sky = value;
                saveConfig();
            }
        ));

        options.add(createSwitchOption(
            "Sun Rendering",
            "vulkanmod-extra.option.details.sun.tooltip",
            () -> VulkanModExtra.CONFIG.detailSettings.sun,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.sun = value;
                saveConfig();
            }
        ));

        options.add(createSwitchOption(
            "Moon Rendering",
            "vulkanmod-extra.option.details.moon.tooltip",
            () -> VulkanModExtra.CONFIG.detailSettings.moon,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.moon = value;
                saveConfig();
            }
        ));

        options.add(createSwitchOption(
            "Stars Rendering",
            "vulkanmod-extra.option.details.stars.tooltip",
            () -> VulkanModExtra.CONFIG.detailSettings.stars,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.stars = value;
                saveConfig();
            }
        ));

        // Weather and environmental effects
        options.add(createSwitchOption(
            "Weather Effects (Rain/Snow)",
            "vulkanmod-extra.option.details.rainSnow.tooltip",
            () -> VulkanModExtra.CONFIG.detailSettings.rainSnow,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.rainSnow = value;
                saveConfig();
            }
        ));

        // Color effects
        options.add(createSwitchOption(
            "Biome Colors",
            "vulkanmod-extra.option.details.biomeColors.tooltip",
            () -> VulkanModExtra.CONFIG.detailSettings.biomeColors,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.biomeColors = value;
                saveConfig();
            }
        ));

        options.add(createSwitchOption(
            "Sky Colors",
            "vulkanmod-extra.option.details.skyColors.tooltip",
            () -> VulkanModExtra.CONFIG.detailSettings.skyColors,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.skyColors = value;
                saveConfig();
            }
        ));

        return createPage("Details", "Detail Settings", options);
    }

    private static Object createRenderPage() throws Exception {
        return createPageWithBlocks("Render", createRenderOptionBlocks());
    }

    private static Object[] createRenderOptionBlocks() throws Exception {
        List<Object> blocks = new ArrayList<>();

        // Basic Render Options Block
        List<Object> renderOptions = new ArrayList<>();
        renderOptions.add(createSwitchOption(
            "Light Updates",
            "vulkanmod-extra.option.render.lightUpdates.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.lightUpdates,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.lightUpdates = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Item Frame Rendering",
            "vulkanmod-extra.option.render.itemFrame.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.itemFrame,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.itemFrame = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Armor Stand Rendering",
            "vulkanmod-extra.option.render.armorStand.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.armorStand,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.armorStand = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Painting Rendering",
            "vulkanmod-extra.option.render.painting.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.painting,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.painting = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Piston Rendering",
            "vulkanmod-extra.option.render.piston.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.piston,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.piston = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Beacon Beam",
            "vulkanmod-extra.option.render.beaconBeam.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.beaconBeam,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.beaconBeam = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Limit Beacon Beam Height",
            "vulkanmod-extra.option.render.limitBeaconBeamHeight.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.limitBeaconBeamHeight,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.limitBeaconBeamHeight = value;
                saveConfig();
            }
        ));

        Object renderBlock = createBlock("Rendering", renderOptions);
        blocks.add(renderBlock);

        // Fog Options Block
        List<Object> fogOptions = new ArrayList<>();
        fogOptions.add(createSwitchOption(
            "Global Fog",
            "vulkanmod-extra.option.render.globalFog.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.globalFog,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.globalFog = value;
                saveConfig();
            }
        ));

        fogOptions.add(createSwitchOption(
            "Multi-Dimension Fog",
            "vulkanmod-extra.option.render.multiDimensionFog.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.multiDimensionFog,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.multiDimensionFog = value;
                saveConfig();
            }
        ));

        Object fogBlock = createBlock("Fog Settings", fogOptions);
        blocks.add(fogBlock);

        return blocks.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, blocks.size()));
    }

    private static Object createExtraPage() throws Exception {
        return createPageWithBlocks("Extra", createExtraOptionBlocks());
    }

    private static Object[] createExtraOptionBlocks() throws Exception {
        List<Object> blockList = new ArrayList<>();

        // Block 1: Display Options (FPS, FPS Mode, Overlay Corner, Text Contrast)
        List<Object> displayOptions = new ArrayList<>();

        displayOptions.add(createSwitchOption(
            "FPS Display",
            "vulkanmod-extra.option.extra.showFps.tooltip",
            () -> VulkanModExtra.CONFIG.extraSettings.showFps,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.showFps = value;
                saveConfig();
            }
        ));

        // FPS Display Mode using CyclingOption
        try {
            Object fpsModeOption = createCyclingOption(
                "FPS Display Mode",
                "vulkanmod-extra.option.extra.fpsDisplayMode.tooltip",
                VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode.getClass().getEnumConstants(),
                () -> VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode,
                (value) -> {
                    VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode = (VulkanModExtraConfig.FPSDisplayMode) value;
                    saveConfig();
                }
            );
            displayOptions.add(fpsModeOption);
        } catch (Exception e) {
            LOGGER.warn("Failed to create FPS mode cycling option", e);
        }

        // Overlay Corner using CyclingOption
        try {
            Object overlayCornerOption = createCyclingOption(
                "Overlay Corner",
                "vulkanmod-extra.option.extra.overlayCorner.tooltip",
                VulkanModExtra.CONFIG.extraSettings.overlayCorner.getClass().getEnumConstants(),
                () -> VulkanModExtra.CONFIG.extraSettings.overlayCorner,
                (value) -> {
                    VulkanModExtra.CONFIG.extraSettings.overlayCorner = (VulkanModExtraConfig.OverlayCorner) value;
                    saveConfig();
                }
            );
            displayOptions.add(overlayCornerOption);
        } catch (Exception e) {
            LOGGER.warn("Failed to create overlay corner cycling option", e);
        }

        // Text Contrast using CyclingOption
        try {
            Object textContrastOption = createCyclingOption(
                "Text Contrast",
                "vulkanmod-extra.option.extra.textContrast.tooltip",
                VulkanModExtra.CONFIG.extraSettings.textContrast.getClass().getEnumConstants(),
                () -> VulkanModExtra.CONFIG.extraSettings.textContrast,
                (value) -> {
                    VulkanModExtra.CONFIG.extraSettings.textContrast = (VulkanModExtraConfig.TextContrast) value;
                    saveConfig();
                }
            );
            displayOptions.add(textContrastOption);
        } catch (Exception e) {
            LOGGER.warn("Failed to create text contrast cycling option", e);
        }

        Object displayBlock = createBlock("Display Settings", displayOptions);
        blockList.add(displayBlock);

        // Block 2: Coordinate Options
        List<Object> coordinateOptions = new ArrayList<>();
        coordinateOptions.add(createSwitchOption(
            "Coordinates Display",
            "vulkanmod-extra.option.extra.showCoords.tooltip",
            () -> VulkanModExtra.CONFIG.extraSettings.showCoords,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.showCoords = value;
                saveConfig();
            }
        ));

        Object coordinateBlock = createBlock("Coordinate Settings", coordinateOptions);
        blockList.add(coordinateBlock);

        // Block 3: Toast Options
        List<Object> toastOptions = new ArrayList<>();
        toastOptions.add(createSwitchOption(
            "Toast Notifications",
            "vulkanmod-extra.option.extra.toasts.tooltip",
            () -> VulkanModExtra.CONFIG.extraSettings.toasts,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.toasts = value;
                saveConfig();
            }
        ));

        toastOptions.add(createSwitchOption(
            "Advancement Toasts",
            "vulkanmod-extra.option.extra.advancementToast.tooltip",
            () -> VulkanModExtra.CONFIG.extraSettings.advancementToast,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.advancementToast = value;
                saveConfig();
            }
        ));

        toastOptions.add(createSwitchOption(
            "Recipe Toasts",
            "vulkanmod-extra.option.extra.recipeToast.tooltip",
            () -> VulkanModExtra.CONFIG.extraSettings.recipeToast,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.recipeToast = value;
                saveConfig();
            }
        ));

        toastOptions.add(createSwitchOption(
            "System Toasts",
            "vulkanmod-extra.option.extra.systemToast.tooltip",
            () -> VulkanModExtra.CONFIG.extraSettings.systemToast,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.systemToast = value;
                saveConfig();
            }
        ));

        toastOptions.add(createSwitchOption(
            "Tutorial Toasts",
            "vulkanmod-extra.option.extra.tutorialToast.tooltip",
            () -> VulkanModExtra.CONFIG.extraSettings.tutorialToast,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.tutorialToast = value;
                saveConfig();
            }
        ));

        Object toastBlock = createBlock("Toast Settings", toastOptions);
        blockList.add(toastBlock);

        // Block 4: Other Options
        List<Object> otherOptions = new ArrayList<>();
        otherOptions.add(createSwitchOption(
            "Instant Sneak",
            "vulkanmod-extra.option.extra.instantSneak.tooltip",
            () -> VulkanModExtra.CONFIG.extraSettings.instantSneak,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.instantSneak = value;
                saveConfig();
            }
        ));

        otherOptions.add(createSwitchOption(
            "Prevent Shaders",
            "vulkanmod-extra.option.render.preventShaders.tooltip",
            () -> VulkanModExtra.CONFIG.extraSettings.preventShaders,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.preventShaders = value;
                saveConfig();
            }
        ));

        otherOptions.add(createSwitchOption(
            "Steady Debug HUD",
            "vulkanmod-extra.option.extra.steadyDebugHud.tooltip",
            () -> VulkanModExtra.CONFIG.extraSettings.steadyDebugHud,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.steadyDebugHud = value;
                saveConfig();
            }
        ));

        Object otherBlock = createBlock("Other Settings", otherOptions);
        blockList.add(otherBlock);

        return blockList.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, blockList.size()));
    }

    private static Object createOptimizationPage() throws Exception {
        return createPageWithBlocks("Optimization", createOptimizationOptionBlocks());
    }

    private static Object[] createOptimizationOptionBlocks() throws Exception {
        List<Object> blocks = new ArrayList<>();

        // Memory Optimization Block (only shown if memory pooling is supported)
        if (com.criticalrange.util.VulkanModVersionHelper.isMemoryPoolingSupported()) {
            List<Object> memoryOptions = new ArrayList<>();

            memoryOptions.add(createSwitchOption(
                "Buffer Pooling",
                "vulkanmod-extra.option.optimization.bufferPooling.tooltip",
                () -> VulkanModExtra.CONFIG.optimizationSettings.bufferPooling,
                (value) -> {
                    VulkanModExtra.CONFIG.optimizationSettings.bufferPooling = value;
                    saveConfig();
                }
            ));

            // Create a range option for buffer pool size
            try {
                // For now, create a switch option - can be upgraded to range slider later
                memoryOptions.add(createSwitchOption(
                    "Large Buffer Pool",
                    "vulkanmod-extra.option.optimization.bufferPoolSize.tooltip",
                    () -> VulkanModExtra.CONFIG.optimizationSettings.bufferPoolSize > 32,
                    (value) -> {
                        VulkanModExtra.CONFIG.optimizationSettings.bufferPoolSize = value ? 128 : 32;
                        saveConfig();
                    }
                ));
            } catch (Exception e) {
                LOGGER.warn("Failed to create buffer pool size option", e);
            }

            Object memoryBlock = createBlock("Memory Optimization", memoryOptions);
            blocks.add(memoryBlock);
        }

        // Add other optimization blocks here as needed
        // (chunk optimization, entity optimization, etc.)

        return blocks.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, blocks.size()));
    }

    // Overloaded method for backward compatibility with String names
    private static Object createSwitchOption(String name, String description, Supplier<Boolean> getter, Consumer<Boolean> setter) throws Exception {
        return createSwitchOption(Text.literal(name), description, getter, setter);
    }

    private static Object createSwitchOption(Text name, String description, Supplier<Boolean> getter, Consumer<Boolean> setter) throws Exception {
        Object switchOption = cachedSwitchOptionClass.getConstructor(Text.class, Consumer.class, Supplier.class)
            .newInstance(
                name,
                setter,
                getter
            );

        // Add tooltip support using translatable text (matching original pattern)
        try {
            Method setTooltipMethod = cachedSwitchOptionClass.getMethod("setTooltip", Text.class);
            // Convert simple description to translatable tooltip key pattern
            Text tooltipText = description.contains(".") ? Text.translatable(description) : Text.literal(description);
            setTooltipMethod.invoke(switchOption, tooltipText);
        } catch (Exception e) {
            LOGGER.debug("No setTooltip method found for SwitchOption, trying alternative methods");
            // Try alternative tooltip methods
            try {
                Method withTooltipMethod = cachedSwitchOptionClass.getMethod("withTooltip", Text.class);
                Text tooltipText = description.contains(".") ? Text.translatable(description) : Text.literal(description);
                switchOption = withTooltipMethod.invoke(switchOption, tooltipText);
            } catch (Exception e2) {
                LOGGER.debug("No tooltip support found for SwitchOption");
            }
        }

        return switchOption;
    }

    private static Object createCyclingOption(String name, String description, Object[] values, Supplier<Object> getter, Consumer<Object> setter) throws Exception {
        Object cyclingOption = cachedCyclingOptionClass.getConstructor(Text.class, Object[].class, Consumer.class, Supplier.class)
            .newInstance(
                Text.literal(name),
                values,
                setter,
                getter
            );

        // Add translator function for enum display names
        try {
            Method setTranslatorMethod = cachedCyclingOptionClass.getMethod("setTranslator", Function.class);
            Function<Object, Text> translator = (value) -> {
                if (value instanceof Enum<?>) {
                    Enum<?> enumValue = (Enum<?>) value;
                    // Convert enum name to display format (e.g., BASIC -> Basic, TOP_LEFT -> Top Left)
                    String enumName = enumValue.name().toLowerCase().replace('_', ' ');
                    StringBuilder displayName = new StringBuilder();
                    boolean capitalize = true;
                    for (char c : enumName.toCharArray()) {
                        if (capitalize && Character.isLetter(c)) {
                            displayName.append(Character.toUpperCase(c));
                            capitalize = false;
                        } else if (c == ' ') {
                            displayName.append(c);
                            capitalize = true;
                        } else {
                            displayName.append(c);
                        }
                    }
                    return Text.literal(displayName.toString());
                }
                return Text.literal(value.toString());
            };
            setTranslatorMethod.invoke(cyclingOption, translator);
        } catch (Exception e) {
            LOGGER.warn("Failed to set translator for cycling option '{}': {}", name, e.getMessage());
        }

        // Add tooltip support using translatable text (matching original pattern)
        try {
            Method setTooltipMethod = cachedCyclingOptionClass.getMethod("setTooltip", Text.class);
            // Convert simple description to translatable tooltip key pattern
            Text tooltipText = description.contains(".") ? Text.translatable(description) : Text.literal(description);
            setTooltipMethod.invoke(cyclingOption, tooltipText);
        } catch (Exception e) {
            LOGGER.debug("No setTooltip method found for CyclingOption, trying alternative methods");
            // Try alternative tooltip methods
            try {
                Method withTooltipMethod = cachedCyclingOptionClass.getMethod("withTooltip", Text.class);
                Text tooltipText = description.contains(".") ? Text.translatable(description) : Text.literal(description);
                cyclingOption = withTooltipMethod.invoke(cyclingOption, tooltipText);
            } catch (Exception e2) {
                LOGGER.debug("No tooltip support found for CyclingOption");
            }
        }

        return cyclingOption;
    }

    private static Object createPage(String name, String blockTitle, List<Object> options) throws Exception {
        // Create array classes for reflection
        Class<?> optionArrayClass = java.lang.reflect.Array.newInstance(cachedOptionClass, 0).getClass();
        Class<?> optionBlockArrayClass = java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 0).getClass();

        // Create options array
        Object[] optionsArray = options.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionClass, options.size()));

        // Create option block
        Object optionBlock = cachedOptionBlockClass.getConstructor(String.class, optionArrayClass)
            .newInstance(blockTitle, optionsArray);

        // Create option blocks array
        Object[] optionBlocks = (Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 1);
        optionBlocks[0] = optionBlock;

        // Create page
        return cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass)
            .newInstance(name, optionBlocks);
    }

    private static Object createPageWithBlocks(String name, Object[] blocks) throws Exception {
        Class<?> optionBlockArrayClass = java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 0).getClass();

        // Create properly typed array
        Object[] typedBlocks = (Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, blocks.length);
        System.arraycopy(blocks, 0, typedBlocks, 0, blocks.length);

        return cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass)
            .newInstance(name, typedBlocks);
    }

    private static Object createBlock(String title, List<Object> options) throws Exception {
        Class<?> optionArrayClass = java.lang.reflect.Array.newInstance(cachedOptionClass, 0).getClass();

        // Create properly typed options array
        Object[] typedOptionsArray = (Object[]) java.lang.reflect.Array.newInstance(cachedOptionClass, options.size());
        for (int i = 0; i < options.size(); i++) {
            typedOptionsArray[i] = options.get(i);
        }

        return cachedOptionBlockClass.getConstructor(String.class, optionArrayClass)
            .newInstance(title, typedOptionsArray);
    }

    private static void saveConfig() {
        try {
            var configManager = com.criticalrange.config.ConfigurationManager.getInstance();
            if (configManager != null) {
                configManager.saveConfig();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to save config", e);
        }
    }

    /**
     * Check if VulkanMod option classes are available
     */
    public static boolean isVulkanModAvailable() {
        return loadVulkanModClasses();
    }
}