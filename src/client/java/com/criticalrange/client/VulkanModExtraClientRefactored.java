package com.criticalrange.client;

import com.criticalrange.core.FeatureManager;
import com.criticalrange.features.animation.AnimationFeature;
import com.criticalrange.features.fps.FPSDisplayFeature;
import com.criticalrange.features.particle.ParticleFeature;
import com.criticalrange.config.ConfigurationManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refactored client-side entry point for VulkanMod Extra
 * Uses the new modular architecture with clean feature management
 */
public class VulkanModExtraClientRefactored implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra Client");
    private static VulkanModExtraClientRefactored instance;

    private FeatureManager featureManager;
    private FPSDisplayFeature fpsDisplayFeature;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing VulkanMod Extra Client (Refactored)...");

        try {
            // Initialize configuration
            ConfigurationManager.getInstance().loadConfig();
            LOGGER.info("Configuration loaded successfully");

            // Initialize feature manager
            featureManager = FeatureManager.getInstance();

            // Register features
            registerFeatures();

            // Initialize features
            MinecraftClient minecraft = MinecraftClient.getInstance();
            featureManager.initializeFeatures(minecraft);

            LOGGER.info("VulkanMod Extra Client initialized successfully!");
            LOGGER.info("Registered {} features", featureManager.getFeatureCount());

        } catch (Exception e) {
            LOGGER.error("Failed to initialize VulkanMod Extra Client", e);
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

        // TODO: Register additional features as they are implemented
        // featureManager.registerFeature(new RenderFeature());
        // featureManager.registerFeature(new HudFeature());
        // featureManager.registerFeature(new PerformanceFeature());
        // featureManager.registerFeature(new EnvironmentFeature());
        // featureManager.registerFeature(new FogFeature());
        // featureManager.registerFeature(new WeatherFeature());
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
    public static void onHudRender(net.minecraft.client.gui.DrawContext drawContext, float partialTicks) {
        if (instance != null && instance.fpsDisplayFeature != null) {
            instance.fpsDisplayFeature.render(drawContext, partialTicks);
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
     * Check if the client is properly initialized
     */
    public static boolean isInitialized() {
        return instance != null && instance.featureManager != null;
    }
}
