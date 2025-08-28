package com.criticalrange.client.config;

public class VulkanModExtraClientConfig {
    // Client-side configuration that extends the main config with Minecraft-specific settings

    // Animation Settings
    public final AnimationSettings animationSettings = new AnimationSettings();
    public final ParticleSettings particleSettings = new ParticleSettings();
    public final DetailSettings detailSettings = new DetailSettings();
    public final RenderSettings renderSettings = new RenderSettings();

    // Animation Settings
    public static class AnimationSettings {
        public boolean animation = true;
        public boolean water = true;
        public boolean lava = true;
        public boolean fire = true;
        public boolean portal = true;
        public boolean blockAnimations = true;
        public boolean sculkSensor = true;
    }

    // Particle Settings
    public static class ParticleSettings {
        public boolean particles = true;
        public boolean rainSplash = true;
        public boolean blockBreak = true;
        public boolean blockBreaking = true;
    }

    // Detail Settings
    public static class DetailSettings {
        public boolean sky = true;
        public boolean sun = true;
        public boolean moon = true;
        public boolean stars = true;
        public boolean rainSnow = true;
        public boolean biomeColors = true;
        public boolean skyColors = true;
    }

    // Render Settings
    public static class RenderSettings {
        public boolean globalFog = true;
        public boolean lightUpdates = true;
        public boolean itemFrame = true;
        public boolean armorStand = true;
        public boolean painting = true;
        public boolean piston = true;
        public boolean beaconBeam = true;
        public boolean limitBeaconBeamHeight = false;
        public boolean enchantingTableBook = true;
        public boolean itemFrameNameTag = true;
        public boolean playerNameTag = true;
    }



    // Static instance for easy access
    private static VulkanModExtraClientConfig instance;

    public static VulkanModExtraClientConfig getInstance() {
        if (instance == null) {
            instance = new VulkanModExtraClientConfig();
        }
        return instance;
    }
}
