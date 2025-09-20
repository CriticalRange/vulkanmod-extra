package com.criticalrange.client;

import com.criticalrange.VulkanModExtra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal VulkanMod integration for fallback support.
 * This class provides essential methods for the hybrid integration system.
 *
 * The main integration is now handled by the event-based system in
 * com.criticalrange.integration.HybridVulkanModIntegration
 */
public class VulkanModExtraIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Integration");

    private static boolean integrationAttempted = false;
    private static volatile boolean isShuttingDown = false;

    /**
     * Minimal integration attempt for backward compatibility
     */
    public static void tryIntegrateWithVulkanMod() {
        if (integrationAttempted || isShuttingDown) {
            return;
        }
        integrationAttempted = true;

        try {
            // Check if VulkanMod is available
            Class.forName("net.vulkanmod.config.gui.VOptionScreen");
            LOGGER.debug("VulkanMod detected - integration available");
        } catch (ClassNotFoundException e) {
            LOGGER.debug("VulkanMod not found - standalone mode");
        }
    }

    /**
     * Creates VulkanMod Extra option pages for integration
     * This is a simplified version that relies on the event system for the actual logic
     */
    public static List<Object> createVulkanModExtraPages() {
        if (isShuttingDown) {
            return new ArrayList<>();
        }

        try {
            // In the new architecture, page creation is handled by the event system
            // This method exists for compatibility with the hybrid system
            LOGGER.debug("Page creation delegated to event-based integration system");
            return new ArrayList<>();
        } catch (Exception e) {
            LOGGER.warn("Error creating VulkanMod Extra pages", e);
            return new ArrayList<>();
        }
    }

    /**
     * Creates VulkanMod Extra option pages with specific typing for mixins
     */
    public static List<net.vulkanmod.config.option.OptionPage> createVulkanModExtraOptionPages() {
        // This method is kept for mixin compatibility but functionality moved to events
        try {
            Class.forName("net.vulkanmod.config.option.OptionPage");
            LOGGER.debug("OptionPage creation delegated to event system");
            return new ArrayList<>();
        } catch (ClassNotFoundException e) {
            LOGGER.debug("VulkanMod OptionPage class not available");
            return new ArrayList<>();
        }
    }

    /**
     * Legacy injection method - now handled by events
     */
    public static void injectPagesIntoCurrentScreen() {
        if (isShuttingDown) {
            return;
        }

        LOGGER.debug("Page injection now handled by event-based integration");
        // Actual injection is performed by the HybridVulkanModIntegration event handlers
    }

    /**
     * Shutdown cleanup
     */
    public static void shutdown() {
        isShuttingDown = true;
        LOGGER.debug("VulkanMod integration fallback shutdown completed");
    }

    /**
     * Check if integration is shutting down
     */
    public static boolean isShuttingDown() {
        return isShuttingDown;
    }
}