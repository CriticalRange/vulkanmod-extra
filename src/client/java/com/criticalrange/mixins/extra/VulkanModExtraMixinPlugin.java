package com.criticalrange.mixins.extra;

import com.criticalrange.util.VersionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Simplified Mixin plugin for VulkanMod Extra with version override support
 * 
 * This plugin provides version detection and mixin configuration support
 * for the override system without complex mixin processing.
 */
public class VulkanModExtraMixinPlugin {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanModExtra Mixin Plugin");
    
    private static String currentVersion;
    private static String currentVersionKey;
    
    static {
        // Initialize version detection
        currentVersion = VersionHelper.getCurrentVersion();
        currentVersionKey = currentVersion.replace('.', '_');
        
        LOGGER.info("[VulkanModExtra] Initializing mixin plugin for Minecraft {}", currentVersion);
        LOGGER.info("[VulkanModExtra] Version key: {}", currentVersionKey);
        
        // Log version information
        VersionHelper.logVersionInfo();
    }
    
    /**
     * Get the current Minecraft version
     */
    public static String getCurrentVersion() {
        return currentVersion;
    }
    
    /**
     * Get the current version key for override directories
     */
    public static String getCurrentVersionKey() {
        return currentVersionKey;
    }
    
    /**
     * Check if version-specific overrides are available
     */
    public static boolean hasVersionOverrides() {
        String overridePath = "src/overrides/v" + currentVersionKey;
        return new File(overridePath).exists();
    }
    
    /**
     * Get the override directory path
     */
    public static String getOverrideDirectory() {
        return "src/overrides/v" + currentVersionKey;
    }
    
    /**
     * Log current configuration for debugging
     */
    public static void logConfiguration() {
        LOGGER.info("[VulkanModExtra] Current Configuration:");
        LOGGER.info("  Minecraft Version: {}", currentVersion);
        LOGGER.info("  Version Key: {}", currentVersionKey);
        LOGGER.info("  Override Directory: {}", getOverrideDirectory());
        LOGGER.info("  Has Overrides: {}", hasVersionOverrides());
    }
}