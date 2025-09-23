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
     * Enum representing different configuration page types
     */
    private enum PageType {
        ANIMATION("animation", "animationSettings"),
        PARTICLE("particle", "particleSettings"),
        DETAIL("details", "detailSettings"),
        RENDER("render", "renderSettings"),
        EXTRA("extra", "extraSettings"),
        OPTIMIZATION("optimization", "optimizationSettings");

        final String keyPrefix;
        final String configFieldName;

        PageType(String keyPrefix, String configFieldName) {
            this.keyPrefix = keyPrefix;
            this.configFieldName = configFieldName;
        }
    }

    /**
     * Get the config object for a specific page type
     */
    private static Object getConfigObject(PageType pageType) {
        switch (pageType) {
            case ANIMATION: return VulkanModExtra.CONFIG.animationSettings;
            case PARTICLE: return VulkanModExtra.CONFIG.particleSettings;
            case DETAIL: return VulkanModExtra.CONFIG.detailSettings;
            case RENDER: return VulkanModExtra.CONFIG.renderSettings;
            case EXTRA: return VulkanModExtra.CONFIG.extraSettings;
            case OPTIMIZATION: return VulkanModExtra.CONFIG.optimizationSettings;
            default: throw new IllegalArgumentException("Unknown page type: " + pageType);
        }
    }

    /**
     * Configuration for creating a unified page
     */
    private static class PageConfig {
        final PageType pageType;
        final String pageTitle;
        final String masterToggleField;
        final String[][] optionGroups;

        PageConfig(PageType pageType, String pageTitle, String masterToggleField, String[][] optionGroups) {
            this.pageType = pageType;
            this.pageTitle = pageTitle;
            this.masterToggleField = masterToggleField;
            this.optionGroups = optionGroups;
        }
    }

    /**
     * Create a VulkanMod option dynamically based on page type and field name
     */
    private static Object createVulkanModOption(PageType pageType, String fieldName) throws Exception {
        Object configObject = getConfigObject(pageType);
        String keyPrefix = "vulkanmod-extra.option." + pageType.keyPrefix;

        // Dynamic type detection - check if field is enum or boolean
        try {
            var field = configObject.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            if (fieldType.isEnum()) {
                // Create cycling option for enum fields
                Object[] enumValues = fieldType.getEnumConstants();
                return createCyclingOption(
                    Text.translatable(keyPrefix + "." + fieldName),
                    keyPrefix + "." + fieldName + ".tooltip",
                    enumValues,
                    () -> getEnumField(configObject, fieldName),
                    (value) -> setEnumField(configObject, fieldName, value)
                );
            } else if (fieldType == int.class || fieldType == Integer.class) {
                // Create range option for integer fields with field-specific ranges
                return createRangeOptionForField(fieldName, configObject, keyPrefix);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                // Create switch option for boolean fields
                return createSwitchOption(
                    Text.translatable(keyPrefix + "." + fieldName),
                    keyPrefix + "." + fieldName + ".tooltip",
                    () -> getBooleanField(configObject, fieldName),
                    (value) -> setBooleanField(configObject, fieldName, value)
                );
            } else {
                LOGGER.warn("Unsupported field type for option '{}': {}", fieldName, fieldType.getSimpleName());
                // Fallback to boolean switch
                return createSwitchOption(
                    Text.translatable(keyPrefix + "." + fieldName),
                    keyPrefix + "." + fieldName + ".tooltip",
                    () -> getBooleanField(configObject, fieldName),
                    (value) -> setBooleanField(configObject, fieldName, value)
                );
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to detect field type for '{}', using boolean switch: {}", fieldName, e.getMessage());
            // Fallback to boolean switch
            return createSwitchOption(
                Text.translatable(keyPrefix + "." + fieldName),
                keyPrefix + "." + fieldName + ".tooltip",
                () -> getBooleanField(configObject, fieldName),
                (value) -> setBooleanField(configObject, fieldName, value)
            );
        }
    }

    /**
     * Create a VulkanMod page with master toggle and grouped options.
     * This is the unified method for creating all types of VulkanMod configuration pages.
     *
     * @param pageType The type of page to create (ANIMATION, PARTICLE, DETAIL, RENDER, EXTRA, OPTIMIZATION)
     * @return The created VulkanMod page object
     */
    public static Object createVulkanModPage(PageType pageType) throws Exception {
        PageConfig config = getPageConfig(pageType);
        List<Object> blocks = new ArrayList<>();

        // Create master toggle block if specified
        if (config.masterToggleField != null) {
            List<Object> masterOptions = new ArrayList<>();
            Object configObject = getConfigObject(config.pageType);
            String keyPrefix = "vulkanmod-extra.option." + config.pageType.keyPrefix;

            masterOptions.add(createSwitchOption(
                Text.translatable(keyPrefix + "." + config.masterToggleField),
                keyPrefix + "." + config.masterToggleField + ".tooltip",
                () -> getBooleanField(configObject, config.masterToggleField),
                (value) -> setBooleanField(configObject, config.masterToggleField, value)
            ));

            Object masterBlock = createBlock("Master Controls", masterOptions);
            blocks.add(masterBlock);
        }

        // Create individual option blocks
        if (config.optionGroups != null && config.optionGroups.length > 0) {
            for (int i = 0; i < config.optionGroups.length; i++) {
                String[] group = config.optionGroups[i];
                List<Object> groupOptions = new ArrayList<>();

                for (String fieldName : group) {
                    groupOptions.add(createVulkanModOption(config.pageType, fieldName));
                }

                // Create a separate block for each group with descriptive names
                String blockName = getBlockName(config.pageType, i);
                Object groupBlock = createBlock(blockName, groupOptions);
                blocks.add(groupBlock);
            }
        }

        return createPage(config.pageTitle, blocks.toArray());
    }

    /**
     * Get descriptive block names for each page type and group
     */
    private static String getBlockName(PageType pageType, int groupIndex) {
        switch (pageType) {
            case RENDER:
                switch (groupIndex) {
                    case 0: return "Entity Rendering";
                    case 1: return "Name Tags";
                    case 2: return "Block Rendering";
                    case 3: return "Light & Effects";
                    case 4: return "Fog Controls";
                    default: return "Additional Controls " + (groupIndex + 1);
                }
            case ANIMATION:
                switch (groupIndex) {
                    case 0: return "Fluid Animations";
                    case 1: return "Fire & Light";
                    case 2: return "Portal Animations";
                    case 3: return "Block Animations";
                    case 4: return "Machine Animations";
                    case 5: return "Plant Animations";
                    case 6: return "Stem Animations";
                    case 7: return "Sculk Animations";
                    case 8: return "Command Blocks";
                    case 9: return "Additional";
                    default: return "Additional Controls " + (groupIndex + 1);
                }
            case PARTICLE:
                switch (groupIndex) {
                    case 0: return "Common Particles";
                    case 1: return "Bubble Effects";
                    case 2: return "Environmental";
                    case 3: return "Combat Effects";
                    case 4: return "Status Effects";
                    case 5: return "Fire & Flame";
                    case 6: return "Liquid Effects";
                    case 7: return "Dust Effects";
                    case 8: return "Explosion Effects";
                    case 9: return "Nature Effects";
                    case 10: return "Sculk Effects";
                    case 11: return "Other Effects";
                    default: return "Additional Controls " + (groupIndex + 1);
                }
            case DETAIL:
                switch (groupIndex) {
                    case 0: return "Sky Elements";
                    case 1: return "Color Effects";
                    default: return "Additional Controls " + (groupIndex + 1);
                }
            case EXTRA:
                switch (groupIndex) {
                    case 0: return "HUD Display";
                    case 1: return "Coordinates";
                    case 2: return "Toast Notifications";
                    default: return "Additional Controls " + (groupIndex + 1);
                }
            default:
                return "Additional Controls " + (groupIndex + 1);
        }
    }

    /**
     * Get the configuration for a specific page type
     */
    private static PageConfig getPageConfig(PageType pageType) {
        switch (pageType) {
            case ANIMATION:
                String[][] animationGroups = {
                    // Fluid animations
                    {"water", "waterStill", "waterFlow", "lava", "lavaStill", "lavaFlow"},
                    // Fire & light animations
                    {"fire", "fire0", "fire1", "soulFire", "soulFire0", "soulFire1", "campfireFire", "soulCampfireFire", "lantern", "soulLantern", "seaLantern"},
                    // Portal animations
                    {"portal", "netherPortal", "endPortal", "endGateway"},
                    // Block animations
                    {"blockAnimations", "magma", "prismarine", "prismarineBricks", "darkPrismarine", "conduit", "respawnAnchor", "stonecutterSaw"},
                    // Machine animations
                    {"machineAnimations", "blastFurnaceFrontOn", "smokerFrontOn", "furnaceFrontOn"},
                    // Plant animations
                    {"plantAnimations", "kelp", "kelpPlant", "seagrass", "tallSeagrassBottom", "tallSeagrassTop"},
                    // Stem animations
                    {"stemAnimations", "warpedStem", "crimsonStem", "warpedHyphae", "crimsonHyphae"},
                    // Sculk animations
                    {"sculkSensor", "sculkSensorTop", "sculkSensorSide", "sculkShrieker", "sculkShriekerTop", "sculkShriekerSide", "calibratedSculkSensor", "calibratedSculkSensorTop", "calibratedSculkSensorSide", "sculkVein", "sculk"},
                    // Command block animations
                    {"commandBlockFront", "chainCommandBlockFront", "repeatingCommandBlockFront"},
                    // Additional animations
                    {"beacon", "dragonEgg", "brewingStandBase", "cauldronWater"}
                };
                return new PageConfig(PageType.ANIMATION, "Animations", "allAnimations", animationGroups);

            case PARTICLE:
                String[][] particleGroups = {
                    // Common particles
                    {"ambientEntityEffect", "barrier", "block", "blockdust", "blockBreaking", "blockMarker"},
                    // Bubble particles
                    {"bubble", "bubbleColumnUp", "bubblePop", "currentDown"},
                    // Environmental particles
                    {"ash", "cherryLeaves", "cloud", "crimsonSpore", "warpedSpore", "whiteAsh", "whiteSmoke"},
                    // Combat particles
                    {"crit", "enchantedHit", "sweepAttack", "damageIndicator"},
                    // Effects particles
                    {"effect", "entityEffect", "instantEffect", "angryVillager", "happyVillager", "heart"},
                    // Fire and flame particles
                    {"flame", "smallFlame", "soulFireFlame", "campfireCosySmoke", "campfireSignalSmoke"},
                    // Liquid particles
                    {"drippingWater", "fallingWater", "landingHoney", "drippingHoney", "fallingHoney", "drippingLava", "fallingLava", "landingLava", "drippingObsidianTear", "fallingObsidianTear", "landingObsidianTear", "drippingDripstoneLava", "fallingDripstoneLava", "drippingDripstoneWater", "fallingDripstoneWater"},
                    // Dust particles
                    {"dust", "dustColorTransition", "dustPillar", "dustPlume", "fallingDust"},
                    // Explosion particles
                    {"explosion", "explosionEmitter", "flash", "firework"},
                    // Nature particles
                    {"fallingSporeBlossom", "sporeBlossomAir", "fallingNectar", "composter"},
                    // Sculk particles
                    {"sculkCharge", "sculkChargePop", "sculkSoul", "shriek", "vibration"},
                    // Other particles
                    {"dolphin", "dragonBreath", "eggCrack", "elderGuardian", "electricSpark", "enchant", "endRod", "fishing", "glow", "glowSquidInk", "gust", "gustEmitterLarge", "gustEmitterSmall", "infested", "item", "itemCobweb", "itemSlime", "itemSnowball", "largeSmoke", "lightBlock", "mycelium", "nautilus", "note", "ominousSpawning", "poof", "portal", "rain", "reversePortal", "scrape", "smallGust", "sneeze", "snowflake", "sonicBoom", "soul", "spit", "splash", "squidInk", "totemOfUndying", "trail", "trialOmen", "trialSpawnerDetection", "trialSpawnerDetectionOminous", "underwater", "vaultConnection", "waxOff", "waxOn", "witch", "wither", "witherArmor"}
                };
                return new PageConfig(PageType.PARTICLE, "Particles", "allParticles", particleGroups);

            case DETAIL:
                String[][] detailGroups = {
                    {"skyGradient", "sun", "moon", "stars", "rainSnow"},
                    {"biomeColors"}
                };
                return new PageConfig(PageType.DETAIL, "Details", "sky", detailGroups);

            case RENDER:
                String[][] renderGroups = {
                    // Entity Rendering
                    {"itemFrame", "armorStand", "painting"},
                    // Name Tags
                    {"itemFrameNameTag", "playerNameTag"},
                    // Block Rendering
                    {"piston"},
                    // Light & Effects
                    {"lightUpdates", "beaconBeam", "beaconBeamHeight"},
                    // Fog Controls
                    {"fog", "overworldFog", "netherFog", "endFog", "waterFog", "lavaFog", "powderSnowFog"}
                };
                return new PageConfig(PageType.RENDER, "Render", null, renderGroups);

            case EXTRA:
                String[][] extraGroups = {
                    {"showFps", "fpsDisplayMode", "overlayCorner", "textContrast"},
                    {"showCoords"},
                    {"toasts", "advancementToast", "recipeToast", "systemToast", "tutorialToast"}
                };
                return new PageConfig(PageType.EXTRA, "Extra", null, extraGroups);

            case OPTIMIZATION:
                String[][] optimizationGroups = {
                    // Reserved for future optimization features
                };
                return new PageConfig(PageType.OPTIMIZATION, "Optimization", null, optimizationGroups);

            default:
                throw new IllegalArgumentException("Unknown page type: " + pageType);
        }
    }


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


            // Save config immediately when field is changed
            saveConfig();

            // Trigger resource reload for animation and particle changes
            if (needsResourceReload(target, fieldName)) {
                triggerResourceReload(fieldName);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to set field: " + fieldName, e);
        }
    }


    /**
     * Get enum field value using reflection
     */
    private static Object getEnumField(Object target, String fieldName) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            LOGGER.warn("Failed to get enum field: " + fieldName, e);
            return null;
        }
    }

    /**
     * Set enum field value using reflection
     */
    private static void setEnumField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
            // Save config immediately when field is changed
            saveConfig();
        } catch (Exception e) {
            LOGGER.warn("Failed to set enum field: " + fieldName, e);
        }
    }



    /**
     * Check if a field change requires resource reload
     */
    private static boolean needsResourceReload(Object target, String fieldName) {
        // Check if target is animation or particle settings
        return target instanceof com.criticalrange.config.VulkanModExtraConfig.AnimationSettings ||
               target instanceof com.criticalrange.config.VulkanModExtraConfig.ParticleSettings;
    }

    /**
     * Trigger Minecraft resource reload
     */
    private static void triggerResourceReload(String reason) {
        try {
            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
            if (minecraft != null) {
                // Schedule reload on main thread to avoid threading issues
                minecraft.execute(() -> {
                    try {
                        LOGGER.info("Triggering resource reload due to {} change", reason);
                        minecraft.reloadResources();
                    } catch (Exception e) {
                        LOGGER.warn("Failed to reload resources: " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to trigger resource reload: " + e.getMessage());
        }
    }

    /**
     * Get integer field value using reflection
     */
    private static Integer getIntegerField(Object target, String fieldName) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(target);
        } catch (Exception e) {
            LOGGER.warn("Failed to get integer field: " + fieldName, e);
            return 0;
        }
    }

    /**
     * Set integer field value using reflection
     */
    private static void setIntegerField(Object target, String fieldName, Integer value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setInt(target, value);
            // Save config immediately when field is changed
            saveConfig();
        } catch (Exception e) {
            LOGGER.warn("Failed to set integer field: " + fieldName, e);
        }
    }

    /**
     * Create range option with field-specific min/max/step values
     */
    private static Object createRangeOptionForField(String fieldName, Object configObject, String keyPrefix) throws Exception {
        // Define field-specific ranges
        int min, max, step;
        switch (fieldName) {
            case "cloudHeight":
                min = 50; max = 500; step = 10;
                break;
            case "cloudDistance":
                min = 50; max = 300; step = 10;
                break;
            case "steadyDebugHudRefreshInterval":
                min = 1; max = 10; step = 1;
                break;
            case "fullscreenMonitor":
                min = 0; max = 5; step = 1;
                break;
            case "beaconBeamHeight":
                min = 32; max = 512; step = 16;
                break;
            default:
                // Generic integer range
                min = 0; max = 100; step = 1;
                LOGGER.warn("Using default range for integer field: {}", fieldName);
        }

        return createRangeOption(
            Text.translatable(keyPrefix + "." + fieldName),
            min, max, step,
            () -> getIntegerField(configObject, fieldName),
            (value) -> setIntegerField(configObject, fieldName, value)
        );
    }

    /**
     * Create a VulkanMod RangeOption
     */
    private static Object createRangeOption(Text name, int min, int max, int step,
                                          Supplier<Integer> getter, Consumer<Integer> setter) throws Exception {
        Object rangeOption = cachedRangeOptionClass.getConstructor(Text.class, int.class, int.class, int.class, Consumer.class, Supplier.class)
            .newInstance(name, min, max, step, setter, getter);

        return rangeOption;
    }

    // Cache VulkanMod classes
    private static Class<?> cachedOptionPageClass;
    private static Class<?> cachedOptionBlockClass;
    private static Class<?> cachedSwitchOptionClass;
    private static Class<?> cachedCyclingOptionClass;
    private static Class<?> cachedRangeOptionClass;
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
                pages.add(createVulkanModPage(PageType.ANIMATION));
                pages.add(createVulkanModPage(PageType.PARTICLE));
                pages.add(createVulkanModPage(PageType.DETAIL));
                pages.add(createVulkanModPage(PageType.RENDER));
                pages.add(createVulkanModPage(PageType.EXTRA));

                // Optimization page removed - no optimization features currently available

                LOGGER.debug("Created {} VulkanMod Extra option pages with settings", pages.size());
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
            cachedRangeOptionClass = Class.forName("net.vulkanmod.config.option.RangeOption");
            cachedOptionClass = Class.forName("net.vulkanmod.config.option.Option");
            classesLoaded = true;
            return true;
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed to load VulkanMod classes for GUI integration", e);
            return false;
        }
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

    // Overloaded method for backward compatibility with String names
    private static Object createCyclingOption(String name, String description, Object[] values, Supplier<Object> getter, Consumer<Object> setter) throws Exception {
        return createCyclingOption(Text.literal(name), description, values, getter, setter);
    }

    private static Object createCyclingOption(Text name, String description, Object[] values, Supplier<Object> getter, Consumer<Object> setter) throws Exception {
        Object cyclingOption = cachedCyclingOptionClass.getConstructor(Text.class, Object[].class, Consumer.class, Supplier.class)
            .newInstance(
                name,
                values,
                setter,
                getter
            );

        // Add translator function for enum display names using translation keys
        try {
            Method setTranslatorMethod = cachedCyclingOptionClass.getMethod("setTranslator", Function.class);
            Function<Object, Text> translator = (value) -> {
                if (value instanceof Enum<?>) {
                    Enum<?> enumValue = (Enum<?>) value;
                    // Convert UPPER_CASE to camelCase for translation keys
                    String enumKey = convertToCamelCase(enumValue.name());
                    String translationKey = description.replace(".tooltip", "." + enumKey);
                    return Text.translatable(translationKey);
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


    /**
     * Create a VulkanMod page from an array of option blocks
     */
    private static Object createPage(String name, Object[] blocks) throws Exception {
        Class<?> optionBlockArrayClass = java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 0).getClass();

        // Create properly typed array
        Object[] typedBlocks = (Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, blocks.length);
        System.arraycopy(blocks, 0, typedBlocks, 0, blocks.length);

        return cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass)
            .newInstance(name, typedBlocks);
    }

    /**
     * Create a VulkanMod option block from a list of options
     */
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
                // Reload the in-memory config to reflect changes immediately
                VulkanModExtra.CONFIG = configManager.loadConfig();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to save config", e);
        }
    }

    /**
     * Convert UPPER_CASE enum names to camelCase for translation keys
     * Examples: TOP_LEFT -> topLeft, BOTTOM_RIGHT -> bottomRight, BASIC -> basic
     */
    private static String convertToCamelCase(String upperCase) {
        if (upperCase == null || upperCase.isEmpty()) {
            return upperCase;
        }

        String[] parts = upperCase.toLowerCase().split("_");
        if (parts.length == 1) {
            return parts[0]; // Single word like "BASIC" -> "basic"
        }

        StringBuilder camelCase = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                camelCase.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    camelCase.append(parts[i].substring(1));
                }
            }
        }
        return camelCase.toString();
    }

    /**
     * Check if VulkanMod option classes are available
     */
    public static boolean isVulkanModAvailable() {
        return loadVulkanModClasses();
    }
}