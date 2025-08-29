package com.criticalrange;

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

		// Register commands for status checking
		registerCommands();

		LOGGER.info("VulkanMod Extra initialized successfully!");
		LOGGER.info("Use '/vulkanmod-extra' command to check feature status");
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("vulkanmod-extra")
				.requires(source -> source.hasPermission(0)) // Allow anyone to use
				.executes(context -> {
					CommandSourceStack source = context.getSource();
					source.sendSuccess(() -> Component.literal("§6=== VulkanMod Extra Status ==="), false);

					// HUD Features
					source.sendSuccess(() -> Component.literal("§eHUD Features:"), false);
					source.sendSuccess(() -> Component.literal("§7  FPS Display: " + (CONFIG.extraSettings.showFps ? "§aEnabled" : "§cDisabled")), false);
					var fpsMode = CONFIG.extraSettings.fpsDisplayMode;
					source.sendSuccess(() -> Component.literal("§7  FPS Mode: §a" + fpsMode.name()), false);
					source.sendSuccess(() -> Component.literal("§7  Coordinates: " + (CONFIG.extraSettings.showCoords ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  Toasts: " + (CONFIG.extraSettings.toasts ? "§aEnabled" : "§cDisabled")), false);

					// Performance Features
					source.sendSuccess(() -> Component.literal("§ePerformance Features:"), false);
					source.sendSuccess(() -> Component.literal("§7  Instant Sneak: " + (CONFIG.extraSettings.instantSneak ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  Adaptive Sync: " + (CONFIG.extraSettings.useAdaptiveSync ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  Steady Debug HUD: " + (CONFIG.extraSettings.steadyDebugHud ? "§aEnabled" : "§cDisabled")), false);

					// Animation & Particles
					source.sendSuccess(() -> Component.literal("§eAnimation & Particles:"), false);
					source.sendSuccess(() -> Component.literal("§7  Animations: " + (CONFIG.animationSettings.animation ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  Particles: " + (CONFIG.particleSettings.particles ? "§aEnabled" : "§cDisabled")), false);

					// Other Features
					source.sendSuccess(() -> Component.literal("§eOther Features:"), false);
					source.sendSuccess(() -> Component.literal("§7  Shader Prevention: " + (CONFIG.extraSettings.preventShaders ? "§aEnabled" : "§cDisabled")), false);
					source.sendSuccess(() -> Component.literal("§7  Mac Resolution Reduction: " + (CONFIG.extraSettings.reduceResolutionOnMac ? "§aEnabled" : "§cDisabled")), false);

					source.sendSuccess(() -> Component.literal("§6Use /vulkanmod-extra reload to reload config"), false);
					source.sendSuccess(() -> Component.literal("§7Note: GUI integration disabled due to VulkanMod conflicts"), false);
					return 1;
				}));

			// Reload command
			dispatcher.register(Commands.literal("vulkanmod-extra")
				.then(Commands.literal("reload")
					.requires(source -> source.hasPermission(2)) // Require OP level 2
					.executes(context -> {
						CONFIG = VulkanModExtraConfig.load();
						context.getSource().sendSuccess(() -> Component.literal("§aVulkanMod Extra config reloaded!"), false);
						return 1;
					})));
		});
	}
}