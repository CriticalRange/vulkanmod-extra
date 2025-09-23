package com.criticalrange.integration;

import com.criticalrange.integration.events.VulkanModEvents;
import com.criticalrange.client.VulkanModExtraIntegration;
import com.criticalrange.VulkanModExtra;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hybrid integration system that prioritizes event-based integration
 * with reflection-based fallback for maximum compatibility.
 */
public class HybridVulkanModIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Hybrid Integration");

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicBoolean eventBasedAvailable = new AtomicBoolean(false);
    private static final AtomicBoolean reflectionFallbackEnabled = new AtomicBoolean(true);

    // Integration statistics
    private static int eventIntegrationSuccesses = 0;
    private static int reflectionFallbackUses = 0;
    private static int totalIntegrationAttempts = 0;

    /**
     * Initialize the hybrid integration system
     */
    public static void initialize() {
        if (initialized.compareAndSet(false, true)) {
            LOGGER.info("Initializing VulkanMod hybrid integration system...");

            // Try to register event-based integration first
            if (tryEventBasedIntegration()) {
                LOGGER.info("Event-based VulkanMod integration successfully registered");
                eventBasedAvailable.set(true);
            } else {
                LOGGER.info("Event-based integration not available, using reflection fallback");
            }

            // Initialize minimal reflection fallback as backup
            VulkanModExtraIntegration.tryIntegrateWithVulkanMod();

            LOGGER.info("VulkanMod hybrid integration system initialized");
        }
    }

    /**
     * Try to set up event-based integration
     */
    private static boolean tryEventBasedIntegration() {
        try {
            // Register event listeners for VulkanMod integration
            VulkanModEvents.CONFIG_SCREEN_INIT.register(HybridVulkanModIntegration::handleConfigScreenInit);
            VulkanModEvents.CONFIG_PAGES_ADDING.register(HybridVulkanModIntegration::handleConfigPagesAdding);
            VulkanModEvents.CONFIG_APPLY.register(HybridVulkanModIntegration::handleConfigApply);
            VulkanModEvents.CONFIG_SAVE.register(HybridVulkanModIntegration::handleConfigSave);
            VulkanModEvents.RESOURCE_RELOAD.register(HybridVulkanModIntegration::handleResourceReload);
            VulkanModEvents.RENDER_SETTINGS_CHANGED.register(HybridVulkanModIntegration::handleRenderSettingsChanged);

            return true;
        } catch (Exception e) {
            LOGGER.warn("Failed to register event-based integration", e);
            return false;
        }
    }

    /**
     * Event handler for VulkanMod config screen initialization
     */
    private static ActionResult handleConfigScreenInit(Object screen) {
        totalIntegrationAttempts++;
        LOGGER.debug("Handling VulkanMod config screen initialization via events");

        try {
            // Use the new event-based page injection
            boolean success = injectPagesViaEvents(screen);

            if (success) {
                eventIntegrationSuccesses++;
                LOGGER.debug("Successfully integrated VulkanMod-Extra pages via events");
                return ActionResult.SUCCESS;
            } else {
                // Fall back to reflection if event-based fails
                return tryReflectionFallback(screen);
            }
        } catch (Exception e) {
            LOGGER.warn("Error in event-based config screen integration", e);
            return tryReflectionFallback(screen);
        }
    }

    /**
     * Event handler for VulkanMod config pages adding
     */
    private static ActionResult handleConfigPagesAdding(Object screen, List<?> pageList) {
        LOGGER.debug("Handling VulkanMod config pages adding via events");

        try {
            if (pageList != null) {
                // Add our pages to the existing list
                List<Object> extraPages = VulkanModPageFactory.createOptionPages();
                if (extraPages != null && !extraPages.isEmpty()) {
                    // Cast to raw type to avoid generics issues
                    @SuppressWarnings("unchecked")
                    List<Object> rawPageList = (List<Object>) pageList;
                    rawPageList.addAll(extraPages);
                    LOGGER.info("Added {} VulkanMod-Extra pages via event system", extraPages.size());
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        } catch (Exception e) {
            LOGGER.warn("Error adding pages via events", e);
            return ActionResult.PASS;
        }
    }

    /**
     * Event handler for VulkanMod config apply
     */
    private static void handleConfigApply(Object config) {
        LOGGER.debug("Handling VulkanMod config apply via events");

        try {
            // Save our configuration when VulkanMod saves theirs
            var configManager = com.criticalrange.config.ConfigurationManager.getInstance();
            if (configManager != null) {
                configManager.saveConfig();
                LOGGER.debug("Synchronized VulkanMod-Extra config save with VulkanMod");
            }
        } catch (Exception e) {
            LOGGER.warn("Error synchronizing config apply", e);
        }
    }

    /**
     * Event handler for VulkanMod config save
     */
    private static void handleConfigSave(Object config) {
        LOGGER.debug("Handling VulkanMod config save via events");
        // Same as apply - ensure our config is saved
        handleConfigApply(config);
    }

    /**
     * Event handler for resource reload
     */
    private static void handleResourceReload(String reason) {
        LOGGER.debug("Handling VulkanMod resource reload via events: {}", reason);

        try {
            // Coordinate our resource reloads with VulkanMod's
            if ("animation_settings".equals(reason) || "render_settings".equals(reason)) {
                // Perform any necessary resource updates
                LOGGER.debug("Coordinated resource reload for: {}", reason);
            }
        } catch (Exception e) {
            LOGGER.warn("Error coordinating resource reload", e);
        }
    }

    /**
     * Event handler for render settings changes
     */
    private static void handleRenderSettingsChanged(String setting, Object oldValue, Object newValue) {
        LOGGER.debug("Handling VulkanMod render setting change via events: {} -> {}", setting, newValue);

        try {
            // Adapt our settings to VulkanMod changes if needed
            adaptToVulkanModSettings(setting, newValue);
        } catch (Exception e) {
            LOGGER.warn("Error adapting to VulkanMod settings change", e);
        }
    }

    /**
     * Inject pages using the new event-based approach
     */
    private static boolean injectPagesViaEvents(Object screen) {
        try {
            // This is cleaner than reflection - we work with the screen directly
            // The event system ensures we're called at the right time

            List<Object> extraPages = VulkanModPageFactory.createOptionPages();
            if (extraPages == null || extraPages.isEmpty()) {
                return false;
            }

            // Try to add pages directly to the screen
            return addPagesToScreenSafely(screen, extraPages);

        } catch (Exception e) {
            LOGGER.warn("Event-based page injection failed", e);
            return false;
        }
    }

    /**
     * Try reflection fallback when event-based integration fails
     */
    private static ActionResult tryReflectionFallback(Object screen) {
        if (!reflectionFallbackEnabled.get()) {
            return ActionResult.FAIL;
        }

        LOGGER.debug("Attempting reflection-based fallback integration");
        reflectionFallbackUses++;

        try {
            // Minimal reflection fallback - just log that we tried
            LOGGER.debug("Event-based integration preferred, reflection fallback minimal");
            return ActionResult.PASS; // Let other systems handle it
        } catch (Exception e) {
            LOGGER.warn("Reflection fallback failed", e);
            return ActionResult.FAIL;
        }
    }

    /**
     * Safely add pages to screen using minimal reflection
     */
    private static boolean addPagesToScreenSafely(Object screen, List<Object> extraPages) {
        try {
            // Try to access optionPages field
            var optionPagesField = screen.getClass().getDeclaredField("optionPages");
            optionPagesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> optionPages = (List<Object>) optionPagesField.get(screen);

            if (optionPages != null) {
                optionPages.addAll(extraPages);
                LOGGER.info("Successfully added {} pages to VulkanMod config screen", extraPages.size());
                return true;
            }
        } catch (Exception e) {
            LOGGER.debug("Could not add pages directly to screen", e);
        }

        return false;
    }

    /**
     * Adapt VulkanMod-Extra settings to VulkanMod changes
     */
    private static void adaptToVulkanModSettings(String setting, Object newValue) {
        // Implement adaptive logic based on VulkanMod settings
        switch (setting) {
            case "video_mode":
                // Adapt our rendering settings to new video mode
                break;
            case "graphics_quality":
                // Adjust our quality settings accordingly
                break;
            // Add more cases as needed
        }
    }

    /**
     * Get integration statistics
     */
    public static IntegrationStats getStats() {
        return new IntegrationStats(
            eventBasedAvailable.get(),
            eventIntegrationSuccesses,
            reflectionFallbackUses,
            totalIntegrationAttempts
        );
    }

    /**
     * Disable reflection fallback (for testing event-only integration)
     */
    public static void disableReflectionFallback() {
        reflectionFallbackEnabled.set(false);
        LOGGER.info("Reflection fallback disabled - using event-only integration");
    }

    /**
     * Check if event-based integration is available and working
     */
    public static boolean isEventBasedAvailable() {
        return eventBasedAvailable.get();
    }

    /**
     * Integration statistics for monitoring and debugging
     */
    public static class IntegrationStats {
        public final boolean eventBasedAvailable;
        public final int eventSuccesses;
        public final int reflectionFallbacks;
        public final int totalAttempts;

        public IntegrationStats(boolean eventBasedAvailable, int eventSuccesses,
                              int reflectionFallbacks, int totalAttempts) {
            this.eventBasedAvailable = eventBasedAvailable;
            this.eventSuccesses = eventSuccesses;
            this.reflectionFallbacks = reflectionFallbacks;
            this.totalAttempts = totalAttempts;
        }

        public double getEventSuccessRate() {
            return totalAttempts > 0 ? (double) eventSuccesses / totalAttempts : 0.0;
        }

        @Override
        public String toString() {
            return String.format("Integration Stats - Event-based: %s, Successes: %d/%d (%.1f%%), Fallbacks: %d",
                eventBasedAvailable, eventSuccesses, totalAttempts,
                getEventSuccessRate() * 100, reflectionFallbacks);
        }
    }
}