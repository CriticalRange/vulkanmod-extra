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
            "Toggle all texture animations on/off",
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
        String displayName = fieldName.replaceAll("([A-Z])", " $1").trim();
        displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);

        return createSwitchOption(
            displayName,
            "Enable/disable " + displayName.toLowerCase(),
            () -> {
                try {
                    var field = VulkanModExtra.CONFIG.animationSettings.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.getBoolean(VulkanModExtra.CONFIG.animationSettings);
                } catch (Exception e) {
                    return true;
                }
            },
            (value) -> {
                try {
                    var field = VulkanModExtra.CONFIG.animationSettings.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.setBoolean(VulkanModExtra.CONFIG.animationSettings, value);
                    saveConfig();
                } catch (Exception e) {
                    LOGGER.warn("Failed to set animation option: " + fieldName, e);
                }
            }
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
            "Toggle all particle effects on/off",
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
        String displayName = fieldName.replaceAll("([A-Z])", " $1").trim();
        displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);

        return createSwitchOption(
            displayName,
            "Enable/disable " + displayName.toLowerCase() + " particles",
            () -> {
                try {
                    var field = VulkanModExtra.CONFIG.particleSettings.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.getBoolean(VulkanModExtra.CONFIG.particleSettings);
                } catch (Exception e) {
                    return true;
                }
            },
            (value) -> {
                try {
                    var field = VulkanModExtra.CONFIG.particleSettings.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.setBoolean(VulkanModExtra.CONFIG.particleSettings, value);
                    saveConfig();
                } catch (Exception e) {
                    LOGGER.warn("Failed to set particle option: " + fieldName, e);
                }
            }
        );
    }

    private static Object createDetailsPage() throws Exception {
        List<Object> options = new ArrayList<>();

        // Sky and celestial elements
        options.add(createSwitchOption(
            "Sky Rendering",
            "Enable/disable sky rendering",
            () -> VulkanModExtra.CONFIG.detailSettings.sky,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.sky = value;
                saveConfig();
            }
        ));

        options.add(createSwitchOption(
            "Sun Rendering",
            "Enable/disable sun rendering",
            () -> VulkanModExtra.CONFIG.detailSettings.sun,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.sun = value;
                saveConfig();
            }
        ));

        options.add(createSwitchOption(
            "Moon Rendering",
            "Enable/disable moon rendering",
            () -> VulkanModExtra.CONFIG.detailSettings.moon,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.moon = value;
                saveConfig();
            }
        ));

        options.add(createSwitchOption(
            "Stars Rendering",
            "Enable/disable stars rendering",
            () -> VulkanModExtra.CONFIG.detailSettings.stars,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.stars = value;
                saveConfig();
            }
        ));

        // Weather and environmental effects
        options.add(createSwitchOption(
            "Weather Effects (Rain/Snow)",
            "Enable/disable weather rendering (rain, snow)",
            () -> VulkanModExtra.CONFIG.detailSettings.rainSnow,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.rainSnow = value;
                saveConfig();
            }
        ));

        // Color effects
        options.add(createSwitchOption(
            "Biome Colors",
            "Enable/disable biome-specific colors",
            () -> VulkanModExtra.CONFIG.detailSettings.biomeColors,
            (value) -> {
                VulkanModExtra.CONFIG.detailSettings.biomeColors = value;
                saveConfig();
            }
        ));

        options.add(createSwitchOption(
            "Sky Colors",
            "Enable/disable sky color variations",
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
            "vulkanmod-extra.option.lightUpdates.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.lightUpdates,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.lightUpdates = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Item Frame Rendering",
            "vulkanmod-extra.option.itemFrame.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.itemFrame,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.itemFrame = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Armor Stand Rendering",
            "vulkanmod-extra.option.armorStand.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.armorStand,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.armorStand = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Painting Rendering",
            "vulkanmod-extra.option.painting.tooltip",
            () -> VulkanModExtra.CONFIG.renderSettings.painting,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.painting = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Piston Rendering",
            "Enable/disable piston rendering optimizations",
            () -> VulkanModExtra.CONFIG.renderSettings.piston,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.piston = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Beacon Beam",
            "Enable/disable beacon beam rendering",
            () -> VulkanModExtra.CONFIG.renderSettings.beaconBeam,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.beaconBeam = value;
                saveConfig();
            }
        ));

        renderOptions.add(createSwitchOption(
            "Limit Beacon Beam Height",
            "Limit beacon beam height for performance",
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
            "Enable/disable fog rendering",
            () -> VulkanModExtra.CONFIG.renderSettings.globalFog,
            (value) -> {
                VulkanModExtra.CONFIG.renderSettings.globalFog = value;
                saveConfig();
            }
        ));

        fogOptions.add(createSwitchOption(
            "Multi-Dimension Fog",
            "Enable/disable multi-dimension fog effects",
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
            "Show FPS counter in game",
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
                "vulkanmod-extra.option.extra.fps_display_mode.tooltip",
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
                "vulkanmod-extra.option.extra.overlay_corner.tooltip",
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
                "vulkanmod-extra.option.extra.text_contrast.tooltip",
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
            "Show coordinate information",
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
            "Enable/disable all toast notifications",
            () -> VulkanModExtra.CONFIG.extraSettings.toasts,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.toasts = value;
                saveConfig();
            }
        ));

        toastOptions.add(createSwitchOption(
            "Advancement Toasts",
            "Enable/disable advancement toast notifications",
            () -> VulkanModExtra.CONFIG.extraSettings.advancementToast,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.advancementToast = value;
                saveConfig();
            }
        ));

        toastOptions.add(createSwitchOption(
            "Recipe Toasts",
            "Enable/disable recipe unlock toast notifications",
            () -> VulkanModExtra.CONFIG.extraSettings.recipeToast,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.recipeToast = value;
                saveConfig();
            }
        ));

        toastOptions.add(createSwitchOption(
            "System Toasts",
            "Enable/disable system toast notifications",
            () -> VulkanModExtra.CONFIG.extraSettings.systemToast,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.systemToast = value;
                saveConfig();
            }
        ));

        toastOptions.add(createSwitchOption(
            "Tutorial Toasts",
            "Enable/disable tutorial toast notifications",
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
            "Enable instant sneaking without animation",
            () -> VulkanModExtra.CONFIG.extraSettings.instantSneak,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.instantSneak = value;
                saveConfig();
            }
        ));

        otherOptions.add(createSwitchOption(
            "Prevent Shaders",
            "Prevent shader loading for better VulkanMod compatibility",
            () -> VulkanModExtra.CONFIG.extraSettings.preventShaders,
            (value) -> {
                VulkanModExtra.CONFIG.extraSettings.preventShaders = value;
                saveConfig();
            }
        ));

        otherOptions.add(createSwitchOption(
            "Steady Debug HUD",
            "Enable stable debug HUD with reduced flicker",
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

    private static Object createSwitchOption(String name, String description, Supplier<Boolean> getter, Consumer<Boolean> setter) throws Exception {
        Object switchOption = cachedSwitchOptionClass.getConstructor(Text.class, Consumer.class, Supplier.class)
            .newInstance(
                Text.literal(name),
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