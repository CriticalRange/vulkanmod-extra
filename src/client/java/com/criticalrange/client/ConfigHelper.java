package com.criticalrange.client;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.config.VulkanModExtraConfig;

/**
 * Configuration access helper for VulkanMod Extra client code.
 * Provides type-safe access to the configuration without polluting the main entry point.
 */
public class ConfigHelper {
    
    /**
     * Get the configuration instance.
     * @return VulkanModExtraConfig instance
     */
    public static VulkanModExtraConfig getConfig() {
        return VulkanModExtra.CONFIG;
    }
    
    /**
     * Set the configuration instance.
     * @param config The configuration instance to set
     */
    public static void setConfig(VulkanModExtraConfig config) {
        VulkanModExtra.CONFIG = config;
    }
    
    /**
     * Check if configuration is initialized.
     * @return true if configuration is initialized
     */
    public static boolean isConfigInitialized() {
        return VulkanModExtra.CONFIG != null;
    }
}