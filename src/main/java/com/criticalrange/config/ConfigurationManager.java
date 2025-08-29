package com.criticalrange.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Centralized configuration management system for VulkanMod Extra
 */
public class ConfigurationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra Config");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static ConfigurationManager instance;
    private final Path configDirectory;
    private VulkanModExtraConfig config;

    private ConfigurationManager() {
        this.configDirectory = FabricLoader.getInstance().getConfigDir().resolve("vulkanmod-extra");
        LOGGER.info("ConfigurationManager initialized with config directory: {}", configDirectory);
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    /**
     * Load or create the configuration
     */
    public VulkanModExtraConfig loadConfig() {
        try {
            Files.createDirectories(configDirectory);

            // Try to load from multiple possible locations
            Path[] possibleConfigFiles = {
                configDirectory.resolve("config.json"),
                configDirectory.getParent().resolve("vulkanmod-extra-config.json"),
                configDirectory.getParent().resolve("vulkanmod-extra-options.json")
            };

            Path configFile = null;
            for (Path possibleFile : possibleConfigFiles) {
                if (Files.exists(possibleFile)) {
                    configFile = possibleFile;
                    LOGGER.info("Found existing config file: {}", possibleFile);
                    break;
                }
            }

            if (configFile != null) {
                try {
                    String json = Files.readString(configFile);
                    config = GSON.fromJson(json, VulkanModExtraConfig.class);
                    if (config == null) {
                        LOGGER.warn("Config file exists but is empty, creating default config");
                        config = new VulkanModExtraConfig();
                    } else {
                        LOGGER.info("Successfully loaded config from: {}", configFile);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to parse config file {}, creating backup and default config", configFile, e);
                    createBackup(configFile);
                    config = new VulkanModExtraConfig();
                }
            } else {
                LOGGER.info("No config file found, creating default config");
                config = new VulkanModExtraConfig();
            }

            saveConfig(); // Save to ensure file exists and is up to date
            return config;

        } catch (Exception e) {
            LOGGER.error("Critical error loading configuration, using defaults", e);
            config = new VulkanModExtraConfig();
            return config;
        }
    }

    /**
     * Save the current configuration
     */
    public void saveConfig() {
        if (config == null) {
            LOGGER.warn("No config to save - config is null");
            return;
        }

        try {
            // Try to save to the intended location first
            Files.createDirectories(configDirectory);
            Path configFile = configDirectory.resolve("config.json");
            String json = GSON.toJson(config);
            Files.writeString(configFile, json);
            LOGGER.info("Configuration saved successfully to: {}", configFile);

            // Also try to save to the root config directory as a fallback
            Path fallbackConfigFile = configDirectory.getParent().resolve("vulkanmod-extra-config.json");
            Files.writeString(fallbackConfigFile, json);
            LOGGER.info("Configuration also saved to fallback location: {}", fallbackConfigFile);

            // Update the static CONFIG reference to ensure all code sees the latest values
            updateStaticConfigReference();

        } catch (IOException e) {
            LOGGER.error("Failed to save configuration: {}", e.getMessage());
            LOGGER.error("Config directory: {}", configDirectory);
            LOGGER.error("Config directory exists: {}", Files.exists(configDirectory));
        }
    }

    /**
     * Update the static CONFIG reference in VulkanModExtra
     */
    private void updateStaticConfigReference() {
        try {
            Class<?> vulkanModExtraClass = Class.forName("com.criticalrange.VulkanModExtra");
            java.lang.reflect.Field configField = vulkanModExtraClass.getDeclaredField("CONFIG");
            configField.setAccessible(true);
            configField.set(null, this.config);
            LOGGER.debug("Updated static CONFIG reference successfully");
        } catch (Exception e) {
            LOGGER.warn("Failed to update static CONFIG reference: {}", e.getMessage());
        }
    }

    /**
     * Get the current configuration
     */
    public VulkanModExtraConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    /**
     * Create a backup of the existing config file
     */
    private void createBackup(Path configFile) {
        try {
            Path backupFile = configFile.resolveSibling("config.json.backup");
            if (Files.exists(configFile)) {
                Files.move(configFile, backupFile);
                LOGGER.info("Created backup: {}", backupFile);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to create backup", e);
        }
    }

    /**
     * Reset configuration to defaults
     */
    public void resetToDefaults() {
        config = new VulkanModExtraConfig();
        saveConfig();
        LOGGER.info("Configuration reset to defaults");
    }

    /**
     * Check if configuration has unsaved changes
     */
    public boolean hasUnsavedChanges() {
        // This would require tracking changes - simplified for now
        return false;
    }
}
