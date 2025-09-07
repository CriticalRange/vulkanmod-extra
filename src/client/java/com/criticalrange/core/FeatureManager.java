package com.criticalrange.core;

import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry and manager for all VulkanMod Extra features.
 * Handles feature lifecycle, registration, and coordination.
 */
public class FeatureManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra Feature Manager");
    private static final FeatureManager INSTANCE = new FeatureManager();

    private final Map<String, Feature> features = new ConcurrentHashMap<>();
    private final Map<FeatureCategory, List<Feature>> featuresByCategory = new ConcurrentHashMap<>();
    private boolean initialized = false;

    private FeatureManager() {
        // Initialize category lists
        for (FeatureCategory category : FeatureCategory.values()) {
            featuresByCategory.put(category, new ArrayList<>());
        }
    }

    public static FeatureManager getInstance() {
        return INSTANCE;
    }

    /**
     * Register a new feature
     */
    public void registerFeature(Feature feature) {
        if (features.containsKey(feature.getId())) {
            LOGGER.warn("Feature with ID '{}' is already registered, skipping", feature.getId());
            return;
        }

        features.put(feature.getId(), feature);
        featuresByCategory.get(feature.getCategory()).add(feature);

        LOGGER.info("Registered feature: {} ({})", feature.getName(), feature.getId());
    }

    /**
     * Get a feature by its ID
     */
    public Optional<Feature> getFeature(String id) {
        return Optional.ofNullable(features.get(id));
    }

    /**
     * Get all features
     */
    public Collection<Feature> getAllFeatures() {
        return features.values();
    }

    /**
     * Get features by category
     */
    public List<Feature> getFeaturesByCategory(FeatureCategory category) {
        return new ArrayList<>(featuresByCategory.get(category));
    }

    /**
     * Get all categories that have features
     */
    public Set<FeatureCategory> getActiveCategories() {
        return featuresByCategory.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Initialize all registered features
     */
    public void initializeFeatures(MinecraftClient minecraft) {
        if (initialized) {
            LOGGER.warn("Features already initialized, skipping");
            return;
        }

        LOGGER.info("Initializing {} features...", features.size());

        for (Feature feature : features.values()) {
            try {
                feature.initialize(minecraft);
                LOGGER.debug("Initialized feature: {}", feature.getName());
            } catch (Exception e) {
                LOGGER.error("Failed to initialize feature: {}", feature.getName(), e);
            }
        }

        initialized = true;
        LOGGER.info("Feature initialization complete");
    }

    /**
     * Tick all enabled features
     */
    public void tickFeatures(MinecraftClient minecraft) {
        if (!initialized) {
            return;
        }

        for (Feature feature : features.values()) {
            if (feature.isEnabled()) {
                try {
                    feature.onTick(minecraft);
                } catch (Exception e) {
                    LOGGER.error("Error ticking feature: {}", feature.getName(), e);
                }
            }
        }
    }

    /**
     * Enable a feature by ID
     */
    public boolean enableFeature(String id) {
        Optional<Feature> featureOpt = getFeature(id);
        if (featureOpt.isPresent()) {
            Feature feature = featureOpt.get();
            if (!feature.isEnabled()) {
                feature.setEnabled(true);
                try {
                    feature.onEnable();
                    LOGGER.info("Enabled feature: {}", feature.getName());
                    return true;
                } catch (Exception e) {
                    LOGGER.error("Error enabling feature: {}", feature.getName(), e);
                    feature.setEnabled(false); // Revert on error
                }
            }
        }
        return false;
    }

    /**
     * Disable a feature by ID
     */
    public boolean disableFeature(String id) {
        Optional<Feature> featureOpt = getFeature(id);
        if (featureOpt.isPresent()) {
            Feature feature = featureOpt.get();
            if (feature.isEnabled()) {
                try {
                    feature.onDisable();
                    feature.setEnabled(false);
                    LOGGER.info("Disabled feature: {}", feature.getName());
                    return true;
                } catch (Exception e) {
                    LOGGER.error("Error disabling feature: {}", feature.getName(), e);
                }
            }
        }
        return false;
    }

    /**
     * Get the count of registered features
     */
    public int getFeatureCount() {
        return features.size();
    }

    /**
     * Get the count of enabled features
     */
    public long getEnabledFeatureCount() {
        return features.values().stream().filter(Feature::isEnabled).count();
    }

    /**
     * Check if a feature is registered
     */
    public boolean isFeatureRegistered(String id) {
        return features.containsKey(id);
    }
}
