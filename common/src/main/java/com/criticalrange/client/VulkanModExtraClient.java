package com.criticalrange.client;

import com.criticalrange.core.FeatureManager;
import com.criticalrange.features.animation.AnimationFeature;
import com.criticalrange.features.particle.ParticleFeature;
import com.criticalrange.features.monitor.MonitorInfoFeature;
import com.criticalrange.config.ConfigurationManager;
import com.criticalrange.VulkanModExtra;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side entry point for VulkanMod Extra
 */
public class VulkanModExtraClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra Client");
    private static VulkanModExtraClient instance;
    private static volatile boolean isShuttingDown = false;

    private FeatureManager featureManager;
    private VulkanModExtraHud hud;
    private long frameCount = 0;

    // Static initializer to register shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(VulkanModExtraClient::shutdown, "VulkanModExtraClient-Shutdown"));
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing VulkanMod Extra Client...");

        try {
            // Configuration is already loaded by main mod initializer
            LOGGER.info("Configuration loaded successfully");

            // Initialize feature manager
            featureManager = FeatureManager.getInstance();

            // Initialize HUD
            hud = new VulkanModExtraHud();


            // Register features
            registerFeatures();

            // Initialize features
            MinecraftClient minecraft = MinecraftClient.getInstance();
            featureManager.initializeFeatures(minecraft);

            // Sync vanilla options with our config
            syncVanillaOptions(minecraft);

            // VulkanMod integration handled through mixins only

            LOGGER.info("VulkanMod Extra Client initialized successfully!");
            LOGGER.info("Registered {} features", featureManager.getFeatureCount());

        } catch (Exception e) {
            LOGGER.error("Failed to initialize VulkanMod Extra Client", e);
        }
    }

    /**
     * Sync vanilla Minecraft options with our config
     */
    private void syncVanillaOptions(MinecraftClient minecraft) {
        // Fast null check and config validation
        if (minecraft == null || minecraft.options == null ||
            VulkanModExtra.CONFIG == null || VulkanModExtra.CONFIG.extraSettings == null) {
            return;
        }

        try {
            minecraft.options.advancedItemTooltips = VulkanModExtra.CONFIG.extraSettings.advancedItemTooltips;
        } catch (Exception e) {
            LOGGER.warn("Failed to sync vanilla options", e);
        }
    }

    /**
     * Register all features with the feature manager
     */
    private void registerFeatures() {
        // Core features
        featureManager.registerFeature(new AnimationFeature());
        featureManager.registerFeature(new ParticleFeature());
        featureManager.registerFeature(new MonitorInfoFeature());
    }

    /**
     * Called every client tick
     */
    public static void onClientTick(MinecraftClient minecraft) {
        if (instance != null && instance.featureManager != null) {
            instance.featureManager.tickFeatures(minecraft);
        }
    }

    /**
     * Called when HUD is rendered
     */
    public static void onHudRender(DrawContext drawContext, float partialTicks) {
        if (instance != null) {
            // Increment frame counter
            instance.frameCount++;

            // Periodic cache cleanup to prevent memory leaks (every 18000 frames = ~5 minutes at 60fps)
            if (instance.frameCount % 18000 == 0) {
                try {
                    com.criticalrange.util.MappingHelper.cleanupCache();
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }

            // Render overlay via HUD only to avoid duplicate FPS text
            if (instance.hud != null) {
                instance.hud.onHudRender(drawContext, partialTicks);
            }
        }
    }

    /**
     * Get the feature manager instance
     */
    public static FeatureManager getFeatureManager() {
        return instance != null ? instance.featureManager : null;
    }



    /**
     * Cleanup resources when mod is shutting down
     */
    public static void onClientShutdown() {
        if (instance != null) {
            LOGGER.info("Shutting down VulkanMod Extra Client...");

            try {
                // Cleanup features
                if (instance.featureManager != null) {
                    instance.featureManager.shutdownFeatures();
                    LOGGER.info("Features shut down");
                }

                // Cleanup static utility caches to prevent memory leaks
                cleanupUtilities();

                // Save configuration
                // Use static reference for faster config save
                if (com.criticalrange.VulkanModExtra.configManager != null) {
                    com.criticalrange.VulkanModExtra.configManager.saveConfig();
                }
                LOGGER.info("Configuration saved");

            } catch (Exception e) {
                LOGGER.error("Error during client shutdown", e);
            }

            instance = null;
            LOGGER.info("VulkanMod Extra Client shutdown complete");
        }
    }

    /**
     * Cleanup utility classes to prevent memory leaks
     */
    private static void cleanupUtilities() {
        try {
            // Cleanup OSHI resources
            com.criticalrange.util.MonitorInfoUtil.cleanup();
            LOGGER.debug("MonitorInfoUtil cleaned up");

            // Cleanup reflection caches
            com.criticalrange.util.MappingHelper.clearCache();
            LOGGER.debug("MappingHelper cache cleared");

        } catch (Exception e) {
            LOGGER.warn("Error cleaning up utilities", e);
        }
    }


    /**
     * Check if the client is properly initialized
     */
    public static boolean isInitialized() {
        return instance != null && instance.featureManager != null;
    }

    /**
     * Coordinated shutdown of all mod components to prevent memory leaks
     */
    public static void shutdown() {
        isShuttingDown = true;
        LOGGER.info("Shutting down VulkanMod Extra Client...");

        try {
            // Shutdown features first
            if (instance != null && instance.featureManager != null) {
                instance.featureManager.shutdownFeatures();
            }

            // Shutdown utilities in order
            com.criticalrange.client.VulkanModExtraIntegration.shutdown();
            com.criticalrange.util.MappingHelper.shutdown();
            com.criticalrange.util.MonitorInfoUtil.shutdown();

            // Cleanup configuration manager
            ConfigurationManager configManager = ConfigurationManager.getInstance();
            if (configManager != null) {
                configManager.saveConfig();
            }

            LOGGER.info("VulkanMod Extra Client shutdown completed");
        } catch (Exception e) {
            LOGGER.error("Error during VulkanMod Extra Client shutdown", e);
        }
    }

    /**
     * Check if the client is shutting down
     * @return true if shutting down
     */
    public static boolean isShuttingDown() {
        return isShuttingDown;
    }
}