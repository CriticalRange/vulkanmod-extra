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
 * Uses single config file: vulkanmod-extra-options.json in root config directory
 * 
 * Configuration changes are only saved when explicitly calling saveConfig().
 * Changes are discarded if the user closes the settings without applying.
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
            // Load from the single config file location
            Path[] possibleConfigFiles = {
                configDirectory.getParent().resolve("vulkanmod-extra-options.json"),
                configDirectory.resolve("config.json"),
                configDirectory.getParent().resolve("vulkanmod-extra-config.json")
            };

            Path configFile = null;
            for (Path possibleFile : possibleConfigFiles) {
                if (Files.exists(possibleFile)) {
                    configFile = possibleFile;
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
        try {
            // Always get the current static CONFIG reference before saving
            // This ensures we save any changes made by the GUI
            VulkanModExtraConfig currentConfig = getCurrentStaticConfig();
            if (currentConfig != null) {
                this.config = currentConfig;
            }

            if (config == null) {
                LOGGER.warn("No config to save - config is null");
                return;
            }

            // Save to the single config file location
            Path configFile = configDirectory.getParent().resolve("vulkanmod-extra-options.json");
            String json = GSON.toJson(config);
            Files.writeString(configFile, json);

            LOGGER.info("Successfully saved config to: {}", configFile);

        } catch (IOException e) {
            LOGGER.error("Failed to save configuration: {}", e.getMessage());
            LOGGER.error("Config directory: {}", configDirectory);
            LOGGER.error("Config directory exists: {}", Files.exists(configDirectory));
        }
    }

    /**
     * Get the current static CONFIG reference from VulkanModExtra
     */
    private VulkanModExtraConfig getCurrentStaticConfig() {
        try {
            Class<?> vulkanModExtraClass = Class.forName("com.criticalrange.VulkanModExtra");
            java.lang.reflect.Field configField = vulkanModExtraClass.getDeclaredField("CONFIG");
            configField.setAccessible(true);
            return (VulkanModExtraConfig) configField.get(null);
        } catch (Exception e) {
            LOGGER.warn("Failed to get current static CONFIG reference: {}", e.getMessage());
            return null;
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
