package com.criticalrange.features.animation;

import com.criticalrange.core.BaseFeature;
import com.criticalrange.core.FeatureCategory;
import com.criticalrange.core.dependency.DependencyType;
import com.criticalrange.core.dependency.FeatureDependency;
import com.criticalrange.core.events.FeatureEvent;
import com.criticalrange.core.events.FeatureEventType;
import com.criticalrange.core.error.ErrorRecoveryStrategy;
import com.criticalrange.core.error.ErrorSeverity;
import com.criticalrange.config.VulkanModExtraConfig;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.Map;

/**
 * Enhanced Animation feature with dependency management, event handling, and error recovery
 * Controls various texture and block animations with automatic recovery
 */
public class AnimationFeature extends BaseFeature {

    public AnimationFeature() {
        super("animations", "Animations", FeatureCategory.ANIMATION,
              "Control texture animations, water, lava, fire, and other animated elements");
        this.version = "2.0.0";
        this.author = "VulkanMod Extra Team";
        this.recoveryStrategy = ErrorRecoveryStrategy.RETRY_DELAYED;
        this.eventPriority = 5;
    }

    @Override
    public List<FeatureDependency> getDependencies() {
        return List.of(
            new FeatureDependency(getId(), "particles", DependencyType.RECOMMENDED,
                "Particle system affects animation performance", true)
        );
    }

    @Override
    protected void doInitialize(MinecraftClient minecraft) {
        getLogger().debug("Animation feature v{} initializing...", version);

        // Register for configuration change events
        registerEventHandlers();

        // Post resource reload event to sync with current config
        postEvent(FeatureEventType.RELOAD_RESOURCES.getEventName(),
                 Map.of("config_sync", true));
    }

    @Override
    public void registerEventHandlers() {
        // Handle configuration changes
        com.criticalrange.core.events.EventBus.getInstance().register(FeatureEventType.CONFIG_CHANGED.getEventName(), event -> {
            String featureId = event.getData("featureId", String.class);
            if (getId().equals(featureId)) {
                String configKey = event.getData("configKey", String.class);
                Object oldValue = event.getData("oldValue", Object.class);
                Object newValue = event.getData("newValue", Object.class);
                onConfigChange(configKey, oldValue, newValue);
            }
            return false; // Don't stop propagation
        }, getEventPriority());

        // Handle resource reloads
        com.criticalrange.core.events.EventBus.getInstance().register(FeatureEventType.RELOAD_RESOURCES.getEventName(), event -> {
            onResourceReload();
            return false; // Don't stop propagation
        }, getEventPriority());

        // Handle performance warnings
        com.criticalrange.core.events.EventBus.getInstance().register(FeatureEventType.PERFORMANCE_WARNING.getEventName(), event -> {
            String warning = event.getData("warning", String.class);
            if (warning != null && warning.contains("animation")) {
                getLogger().warn("Performance warning for animations: {}", warning);
            }
            return false; // Don't stop propagation
        }, getEventPriority());
    }

    @Override
    public boolean isEnabled() {
        return handleError("is_enabled_check", () -> {
            VulkanModExtraConfig config = getConfig();
            return config != null && enabled;
        }, false);
    }

    /**
     * Enhanced animation check with error recovery
     */
    public boolean isAnimationEnabled(String animationName) {
        return handleError("animation_check_" + animationName, () -> {
            if (!isEnabled()) {
                return false;
            }

            VulkanModExtraConfig config = getConfig();
            if (config == null) {
                getLogger().debug("No config available for animation '{}', defaulting to enabled", animationName);
                return true;
            }

            VulkanModExtraConfig.AnimationSettings settings = config.animationSettings;

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
                    getLogger().debug("Unknown animation type: {} - defaulting to enabled", animationName);
                    yield true;
                }
            };
        }, true, ErrorSeverity.WARNING);
    }

    @Override
    public void onConfigChange(String configKey, Object oldValue, Object newValue) {
        if (configKey != null && configKey.startsWith("animationSettings.")) {
            getLogger().info("Animation setting changed: {} = {}", configKey, newValue);

            // Post a performance warning if many animations are disabled
            if (configKey.equals("animationSettings.allAnimations") && Boolean.FALSE.equals(newValue)) {
                postPerformanceWarning("Most animations disabled",
                                     Map.of("config_key", configKey, "old_value", oldValue, "new_value", newValue));
            }
        }
    }

    @Override
    public void onResourceReload() {
        getLogger().debug("Resource reload - refreshing animation settings");
        // Animation settings will be refreshed on next access
    }

    @Override
    public boolean handleEvent(FeatureEvent event) {
        if (event.getEventType().equals(FeatureEventType.WORLD_LOAD.getEventName())) {
            // Reset any animation state when world loads
            getLogger().debug("World loaded - resetting animation state");
            return true;
        }
        return false;
    }

    @Override
    public boolean performHealthCheck() {
        return handleError("animation_health_check", () -> {
            boolean basicHealth = isEnabled() && getConfig() != null;

            if (basicHealth) {
                // Check if critical animations are accessible
                try {
                    boolean waterCheck = isAnimationEnabled("water");
                    boolean fireCheck = isAnimationEnabled("fire");
                    boolean portalCheck = isAnimationEnabled("portal");

                    if (!waterCheck && !fireCheck && !portalCheck) {
                        getLogger().warn("All critical animations are disabled");
                        return false;
                    }
                } catch (Exception e) {
                    getLogger().error("Error checking animation states", e);
                    return false;
                }
            }

            return basicHealth;
        }, false);
    }

    @Override
    public String getDiagnosticInfo() {
        return handleError("diagnostic_info", () -> {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Animation Feature v%s by %s\n", version, author));
            sb.append(String.format("Status: %s, Initialized: %b\n", enabled ? "Enabled" : "Disabled", getInitialized().get()));
            sb.append(String.format("Category: %s, Priority: %d\n", category.name(), eventPriority));
            sb.append(String.format("Recovery Strategy: %s\n", recoveryStrategy.getStrategyName()));

            if (getConfig() != null) {
                sb.append("Config: Accessible\n");
                // Count enabled animations
                long enabledCount = 0;
                long totalCount = 0;

                try {
                    enabledCount = countEnabledAnimations();
                    totalCount = countTotalAnimations();
                } catch (Exception e) {
                    sb.append("Error counting animations\n");
                }

                sb.append(String.format("Animations: %d/%d enabled\n", enabledCount, totalCount));
            } else {
                sb.append("Config: Not accessible\n");
            }

            return sb.toString();
        }, "Animation Feature - Diagnostic info unavailable", ErrorSeverity.INFO);
    }

    /**
     * Count enabled animations
     */
    private long countEnabledAnimations() {
        // Simplified count - in reality you'd check all known animation types
        return List.of("water", "lava", "fire", "portal", "block_animations")
                  .stream()
                  .filter(this::isAnimationEnabled)
                  .count();
    }

    /**
     * Count total animations
     */
    private long countTotalAnimations() {
        // Simplified count
        return 5; // water, lava, fire, portal, block_animations
    }

    // Legacy compatibility methods with enhanced error handling
    public boolean isWaterAnimationEnabled() {
        return handleError("legacy_water_check", () -> isAnimationEnabled("water"), true);
    }

    public boolean isLavaAnimationEnabled() {
        return handleError("legacy_lava_check", () -> isAnimationEnabled("lava"), true);
    }

    public boolean isFireAnimationEnabled() {
        return handleError("legacy_fire_check", () -> isAnimationEnabled("fire"), true);
    }

    public boolean isPortalAnimationEnabled() {
        return handleError("legacy_portal_check", () -> isAnimationEnabled("portal"), true);
    }

    public boolean isBlockAnimationEnabled() {
        return handleError("legacy_block_check", () -> isAnimationEnabled("block_animations"), true);
    }

    public boolean isTextureAnimationEnabled() {
        return handleError("legacy_texture_check", () -> {
            // Individual animation types control their own behavior
            return true;
        }, true);
    }

    public boolean isSculkSensorAnimationEnabled() {
        return handleError("legacy_sculk_check", () -> isAnimationEnabled("sculk_sensor"), true);
    }

    /**
     * Get animation settings for external use with error handling
     */
    public VulkanModExtraConfig.AnimationSettings getAnimationSettings() {
        return handleError("get_animation_settings", () -> {
            VulkanModExtraConfig config = getConfig();
            return config != null ? config.animationSettings : new VulkanModExtraConfig.AnimationSettings();
        }, new VulkanModExtraConfig.AnimationSettings());
    }

    /**
     * Safely enable a specific animation type
     */
    public boolean enableAnimation(String animationName) {
        return handleError("enable_animation_" + animationName, () -> {
            VulkanModExtraConfig config = getConfig();
            if (config == null) return false;

            // This would require reflection or config modification API
            // For now, this is a placeholder
            getLogger().info("Enable animation '{}' requested", animationName);
            return true;
        }, false);
    }

    /**
     * Safely disable a specific animation type
     */
    public boolean disableAnimation(String animationName) {
        return handleError("disable_animation_" + animationName, () -> {
            VulkanModExtraConfig config = getConfig();
            if (config == null) return false;

            // This would require reflection or config modification API
            // For now, this is a placeholder
            getLogger().info("Disable animation '{}' requested", animationName);
            return true;
        }, false);
    }
}
