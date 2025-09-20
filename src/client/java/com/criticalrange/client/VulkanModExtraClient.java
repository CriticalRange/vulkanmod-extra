package com.criticalrange.client;

import com.criticalrange.core.FeatureManager;
import com.criticalrange.features.animation.AnimationFeature;
import com.criticalrange.features.fps.FPSDisplayFeature;
import com.criticalrange.features.particle.ParticleFeature;
import com.criticalrange.features.monitor.MonitorInfoFeature;
import com.criticalrange.config.ConfigurationManager;
import com.criticalrange.optimization.BufferPool;
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

    private FeatureManager featureManager;
    private FPSDisplayFeature fpsDisplayFeature;
    private VulkanModExtraHud hud;
    private BufferPool bufferPool;
    private long frameCount = 0;

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

            // Initialize buffer pool
            initializeBufferPool();

            // Register features
            registerFeatures();

            // Initialize features
            MinecraftClient minecraft = MinecraftClient.getInstance();
            featureManager.initializeFeatures(minecraft);

            // Sync vanilla options with our config
            syncVanillaOptions(minecraft);

            // Try to integrate with VulkanMod's GUI system
            try {
                VulkanModExtraIntegration.tryIntegrateWithVulkanMod();
            } catch (Exception e) {
                LOGGER.warn("Failed to integrate with VulkanMod GUI, but features will still work", e);
            }

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
        fpsDisplayFeature = new FPSDisplayFeature();
        featureManager.registerFeature(fpsDisplayFeature);

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
            // Increment frame counter for buffer pool cleanup
            instance.frameCount++;

            // Perform frame-based buffer pool cleanup
            if (instance.bufferPool != null) {
                instance.bufferPool.onFrameRender(instance.frameCount);
            }

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
     * Get the FPS display feature
     */
    public static FPSDisplayFeature getFpsDisplayFeature() {
        return instance != null ? instance.fpsDisplayFeature : null;
    }

    /**
     * Initialize buffer pool for optimization
     */
    private void initializeBufferPool() {
        try {
            bufferPool = BufferPool.getInstance();

            // Verify buffer pool is properly configured
            if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.optimizationSettings != null) {
                boolean poolingEnabled = VulkanModExtra.CONFIG.optimizationSettings.bufferPooling;
                int poolSizeMB = VulkanModExtra.CONFIG.optimizationSettings.bufferPoolSize;
                boolean isActive = bufferPool.isActive();

                LOGGER.info("Buffer pool initialized successfully - enabled: {}, size: {}MB, active: {}",
                    poolingEnabled, poolSizeMB, isActive);

                if (!poolingEnabled) {
                    LOGGER.info("Buffer pooling is disabled in configuration");
                } else if (isActive) {
                    LOGGER.info("Buffer pooling is ACTIVE and ready to optimize VulkanMod buffer allocations");
                } else {
                    LOGGER.warn("Buffer pooling is configured but not active - check configuration");
                }
            } else {
                LOGGER.warn("Buffer pool configuration not available, using defaults");
                // Force enable for testing if no config available
                bufferPool.forceEnable();
            }

        } catch (Exception e) {
            LOGGER.error("Failed to initialize buffer pool", e);
        }
    }

    /**
     * Cleanup resources when mod is shutting down
     */
    public static void onClientShutdown() {
        if (instance != null) {
            LOGGER.info("Shutting down VulkanMod Extra Client...");

            try {
                // Cleanup buffer pool
                if (instance.bufferPool != null) {
                    instance.bufferPool.cleanup();
                    LOGGER.info("Buffer pool cleaned up");
                }

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
     * Get buffer pool instance
     */
    public static BufferPool getBufferPool() {
        return instance != null ? instance.bufferPool : null;
    }

    /**
     * Check if the client is properly initialized
     */
    public static boolean isInitialized() {
        return instance != null && instance.featureManager != null;
    }
}