package com.criticalrange;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.criticalrange.config.VulkanModExtraConfig;
import com.criticalrange.config.ConfigurationManager;

/**
 * Main entry point for VulkanMod Extra.
 * This is a client-only mod, so most functionality is initialized in the client entry point.
 */
public class VulkanModExtra implements ModInitializer {
	public static final String MOD_ID = "vulkanmod-extra";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Configuration instance - will be initialized by client code
	public static VulkanModExtraConfig CONFIG;
	
	// Configuration manager for handling config operations
	public static ConfigurationManager configManager;

	@Override
	public void onInitialize() {
		// Initialize configuration system
		configManager = ConfigurationManager.getInstance();
		CONFIG = configManager.loadConfig();
		
	}
	
	private int countSettings(VulkanModExtraConfig config) {
		int count = 0;
		if (config.renderSettings != null) count++;
		if (config.detailSettings != null) count++;
		if (config.extraSettings != null) count++;
		if (config.animationSettings != null) count++;
		if (config.particleSettings != null) count++;
		return count;
	}
}