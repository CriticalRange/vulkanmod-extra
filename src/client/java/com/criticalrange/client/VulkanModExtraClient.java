package com.criticalrange.client;

import com.criticalrange.core.FeatureManager;
import com.criticalrange.features.animation.AnimationFeature;
import com.criticalrange.features.fps.FPSDisplayFeature;
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

    private FeatureManager featureManager;
    private FPSDisplayFeature fpsDisplayFeature;
    private VulkanModExtraHud hud;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing VulkanMod Extra Client...");

        try {
            // Initialize configuration
            ConfigurationManager.getInstance().loadConfig();
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
        try {
            if (minecraft != null && minecraft.options != null) {
                // Sync advanced item tooltips - apply our config to vanilla option
                minecraft.options.advancedItemTooltips = VulkanModExtra.CONFIG.extraSettings.advancedItemTooltips;
                LOGGER.debug("Synced advanced item tooltips: {}", minecraft.options.advancedItemTooltips);
            }
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
            // Render features HUD
            if (instance.fpsDisplayFeature != null) {
                instance.fpsDisplayFeature.render(drawContext, partialTicks);
            }
            
            // Render legacy HUD
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
     * Check if the client is properly initialized
     */
    public static boolean isInitialized() {
        return instance != null && instance.featureManager != null;
    }
}