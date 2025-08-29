package com.criticalrange;

import com.criticalrange.config.ConfigurationManager;
import com.criticalrange.config.VulkanModExtraConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VulkanModExtra implements ModInitializer {
	public static final String MOD_ID = "vulkanmod-extra";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Use new configuration manager
	private static ConfigurationManager configManager;
	public static VulkanModExtraConfig CONFIG;

	// Reference to the Config class for external access to enums
	public static final Class<?> Config = VulkanModExtraConfig.class;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Initializing VulkanMod Extra...");

		// Initialize configuration system
		configManager = ConfigurationManager.getInstance();
		CONFIG = configManager.loadConfig();

		// Register commands for status checking
		registerCommands();

		LOGGER.info("VulkanMod Extra initialized successfully!");
		LOGGER.info("Use '/vulkanmod-extra' command to check feature status");
		LOGGER.info("Configuration loaded from: {}", configManager.getConfig().toString());
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("vulkanmod-extra")
				.requires(source -> source.hasPermission(0)) // Allow anyone to use
				.executes(context -> {
					CommandSourceStack source = context.getSource();
					source.sendSuccess(() -> Component.literal("§6=== VulkanMod Extra Status ==="), false);

					// Core Features
					source.sendSuccess(() -> Component.literal("§eCore Features:"), false);
					source.sendSuccess(() -> Component.literal("§7  Mod Enabled: " + (CONFIG.coreSettings.enableMod ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  GUI Integration: " + (CONFIG.coreSettings.enableGuiIntegration ? "§aEnabled" : "§cDisabled")), false);

					// HUD Features
					source.sendSuccess(() -> Component.literal("§eHUD Features:"), false);
					source.sendSuccess(() -> Component.literal("§7  FPS Display: " + (CONFIG.hudSettings.showFps ? "§aEnabled" : "§cDisabled")), false);
					var fpsMode = CONFIG.hudSettings.fpsDisplayMode;
					source.sendSuccess(() -> Component.literal("§7  FPS Mode: §a" + fpsMode.name()), false);
					source.sendSuccess(() -> Component.literal("§7  Coordinates: " + (CONFIG.hudSettings.showCoords ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  Toasts: " + (CONFIG.hudSettings.toasts ? "§aEnabled" : "§cDisabled")), false);

					// Performance Features
					source.sendSuccess(() -> Component.literal("§ePerformance Features:"), false);
					source.sendSuccess(() -> Component.literal("§7  Instant Sneak: " + (CONFIG.performanceSettings.instantSneak ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  Adaptive Sync: " + (CONFIG.performanceSettings.useAdaptiveSync ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  Steady Debug HUD: " + (CONFIG.performanceSettings.steadyDebugHud ? "§aEnabled" : "§cDisabled")), false);

					// Animation & Particles
					source.sendSuccess(() -> Component.literal("§eAnimation & Particles:"), false);
					source.sendSuccess(() -> Component.literal("§7  Animations: " + (CONFIG.animationSettings.animations ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  Particles: " + (CONFIG.particleSettings.particles ? "§aEnabled" : "§cDisabled")), false);

					// Environment Features
					source.sendSuccess(() -> Component.literal("§eEnvironment Features:"), false);
					source.sendSuccess(() -> Component.literal("§7  Sky Rendering: " + (CONFIG.environmentSettings.sky ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  Weather Effects: " + (CONFIG.environmentSettings.rainSnow ? "§aEnabled" : "§cDisabled")), false);

					source.sendSuccess(() -> Component.literal("§6Use /vulkanmod-extra reload to reload config"), false);
					source.sendSuccess(() -> Component.literal("§7Note: Refactored architecture - some legacy features may be limited"), false);
					return 1;
				}));

			// Reload command
			dispatcher.register(Commands.literal("vulkanmod-extra")
				.then(Commands.literal("reload")
					.requires(source -> source.hasPermission(2)) // Require OP level 2
					.executes(context -> {
						CONFIG = configManager.loadConfig();
						context.getSource().sendSuccess(() -> Component.literal("§aVulkanMod Extra config reloaded!"), false);
						return 1;
					})));
		});
	}
}