package com.criticalrange.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.criticalrange.VulkanModExtra;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

public class VulkanModExtraConfig {
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("vulkanmod-extra-options.json");

    // Constructor to initialize settings
    public VulkanModExtraConfig() {
        this.animationSettings = new AnimationSettings();
        this.particleSettings = new ParticleSettings();
    }

    // Settings categories - basic settings that don't require Minecraft classes
    public final ExtraSettings extraSettings = new ExtraSettings();
    public AnimationSettings animationSettings;
    public ParticleSettings particleSettings;

    public static VulkanModExtraConfig load() {
        VulkanModExtraConfig config;

        if (Files.exists(CONFIG_PATH)) {
            try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
                config = GSON.fromJson(reader, VulkanModExtraConfig.class);
            } catch (Exception e) {
                VulkanModExtra.LOGGER.error("Could not parse config, falling back to defaults!", e);
                config = new VulkanModExtraConfig();
            }
        } else {
            config = new VulkanModExtraConfig();
        }

        // Initialize settings if they weren't loaded from file
        if (config.animationSettings == null) {
            config.animationSettings = new AnimationSettings();
        }
        if (config.particleSettings == null) {
            config.particleSettings = new ParticleSettings();
        }

        config.writeChanges();
        return config;
    }

    public void writeChanges() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            VulkanModExtra.LOGGER.error("Could not save configuration file", e);
        }
    }

    // Extra Settings - basic settings available on all platforms
    public static class ExtraSettings {
        public OverlayCorner overlayCorner = OverlayCorner.TOP_LEFT;
        public TextContrast textContrast = TextContrast.NONE;
        public boolean showFps = false;
        public boolean showFPSExtended = true;
        public boolean showCoords = false;
        public boolean reduceResolutionOnMac = false;
        public boolean useAdaptiveSync = false;
        public int cloudHeight = 192;
        public int cloudDistance = 100;
        public boolean toasts = true;
        public boolean advancementToast = true;
        public boolean recipeToast = true;
        public boolean systemToast = true;
        public boolean tutorialToast = true;
        public boolean instantSneak = false;
        public boolean preventShaders = false;
        public boolean steadyDebugHud = true;
        public int steadyDebugHudRefreshInterval = 1;
    }

    // Animation Settings - control texture animations
    public static class AnimationSettings {
        public boolean animation = true;
        public boolean water = false; // Disabled by default for performance
        public boolean lava = false; // Disabled by default for performance
        public boolean fire = false; // Disabled by default for performance
        public boolean portal = false; // Disabled by default for performance
        public boolean blockAnimations = true;
        public boolean sculkSensor = true;
    }

    // Particle Settings - control particle effects
    public static class ParticleSettings {
        public boolean particles = true;
        public boolean rainSplash = true;
        public boolean blockBreak = true;
        public boolean blockBreaking = true;
        public boolean flame = true;
        public boolean smoke = true;
        public boolean campfireSmoke = true;
        public boolean bubble = true;
        public boolean splash = true;
        public boolean rain = true;
        public boolean drippingWater = true;
    }

    public enum OverlayCorner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public enum TextContrast {
        NONE, BACKGROUND, SHADOW
    }
}
