package com.criticalrange.core;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.MinecraftClient;

/**
 * Abstract base class for features providing common functionality
 */
public abstract class BaseFeature implements Feature {
    protected final String id;
    protected final String name;
    protected final FeatureCategory category;
    protected boolean enabled = true; // Default to enabled for compatibility
    protected String description = "No description available";

    protected BaseFeature(String id, String name, FeatureCategory category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    protected BaseFeature(String id, String name, FeatureCategory category, String description) {
        this(id, name, category);
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public FeatureCategory getCategory() {
        return category;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Get the logger for this feature
     */
    protected org.slf4j.Logger getLogger() {
        return org.slf4j.LoggerFactory.getLogger("VulkanMod Extra - " + name);
    }

    /**
     * Check if we're on the client side
     */
    protected boolean isClientSide() {
        return MinecraftClient.getInstance() != null;
    }

    /**
     * Safe way to get Minecraft instance with null checks
     */
    protected MinecraftClient getMinecraft() {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft == null) {
            getLogger().warn("MinecraftClient instance is null!");
        }
        return minecraft;
    }

    /**
     * Get the main config instance
     */
    protected com.criticalrange.config.VulkanModExtraConfig getConfig() {
        return VulkanModExtra.CONFIG;
    }

    /**
     * Mark config as changed (will be saved)
     * Optimized to avoid unnecessary null checks
     */
    protected void markConfigChanged() {
        VulkanModExtra.CONFIG.writeChanges();
    }
}
