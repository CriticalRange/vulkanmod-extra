package com.criticalrange;

import com.criticalrange.config.VulkanModExtraConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VulkanModExtra implements ModInitializer {
	public static final String MOD_ID = "vulkanmod-extra";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static VulkanModExtraConfig CONFIG;

	// Reference to the Config class for external access to enums
	public static final Class<?> Config = VulkanModExtraConfig.class;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Initializing VulkanMod Extra...");

		CONFIG = VulkanModExtraConfig.load();

		LOGGER.info("VulkanMod Extra initialized successfully!");
	}
}