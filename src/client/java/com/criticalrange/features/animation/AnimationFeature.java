package com.criticalrange.features.animation;

import com.criticalrange.core.BaseFeature;
import com.criticalrange.core.FeatureCategory;
import com.criticalrange.config.VulkanModExtraConfig;
import net.minecraft.client.Minecraft;

/**
 * Animation feature - controls various texture and block animations
 */
public class AnimationFeature extends BaseFeature {

    public AnimationFeature() {
        super("animations", "Animations", FeatureCategory.ANIMATION,
              "Control texture animations, water, lava, fire, and other animated elements");
    }

    @Override
    public void initialize(Minecraft minecraft) {
        getLogger().info("Animation feature initialized");
    }

    @Override
    public boolean isEnabled() {
        VulkanModExtraConfig config = getConfig();
        return config != null && enabled;
    }

    /**
     * Check if water animation is enabled
     */
    public boolean isWaterAnimationEnabled() {
        VulkanModExtraConfig config = getConfig();
        return config != null && config.animationSettings.water;
    }

    /**
     * Check if lava animation is enabled
     */
    public boolean isLavaAnimationEnabled() {
        VulkanModExtraConfig config = getConfig();
        return config != null && config.animationSettings.lava;
    }

    /**
     * Check if fire animation is enabled
     */
    public boolean isFireAnimationEnabled() {
        VulkanModExtraConfig config = getConfig();
        return config != null && config.animationSettings.fire;
    }

    /**
     * Check if portal animation is enabled
     */
    public boolean isPortalAnimationEnabled() {
        VulkanModExtraConfig config = getConfig();
        return config != null && config.animationSettings.portal;
    }

    /**
     * Check if block animations are enabled
     */
    public boolean isBlockAnimationEnabled() {
        VulkanModExtraConfig config = getConfig();
        return config != null && config.animationSettings.blockAnimations;
    }

    /**
     * Check if texture animations are enabled
     */
    public boolean isTextureAnimationEnabled() {
        VulkanModExtraConfig config = getConfig();
        return config != null && config.animationSettings.textureAnimations;
    }

    /**
     * Check if sculk sensor animation is enabled
     */
    public boolean isSculkSensorAnimationEnabled() {
        VulkanModExtraConfig config = getConfig();
        return config != null && config.animationSettings.sculkSensor;
    }

    /**
     * Get animation settings for external use
     */
    public VulkanModExtraConfig.AnimationSettings getAnimationSettings() {
        VulkanModExtraConfig config = getConfig();
        return config != null ? config.animationSettings : new VulkanModExtraConfig.AnimationSettings();
    }
}
