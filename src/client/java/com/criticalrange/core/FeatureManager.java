package com.criticalrange.core;

import com.criticalrange.core.dependency.DependencyGraph;
import com.criticalrange.core.dependency.DependencyType;
import com.criticalrange.core.dependency.FeatureDependency;
import com.criticalrange.core.error.ErrorSeverity;
import com.criticalrange.core.events.EventBus;
import com.criticalrange.core.events.FeatureEvent;
import com.criticalrange.core.events.FeatureEventType;
import com.criticalrange.core.error.ErrorRecoveryManager;
import com.criticalrange.core.error.ErrorRecoveryStrategy;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Enhanced central registry and manager for all VulkanMod Extra features.
 * Handles feature lifecycle, dependency management, event coordination, and error recovery.
 */
public class FeatureManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra Feature Manager");
    private static final FeatureManager INSTANCE = new FeatureManager();

    private final Map<String, Feature> features = new ConcurrentHashMap<>();
    private final Map<FeatureCategory, List<Feature>> featuresByCategory = new ConcurrentHashMap<>();
    private final DependencyGraph dependencyGraph = DependencyGraph.getInstance();
    private final EventBus eventBus = EventBus.getInstance();
    private final ErrorRecoveryManager errorRecoveryManager = ErrorRecoveryManager.getInstance();
    private boolean initialized = false;
    private List<String> loadingOrder = new CopyOnWriteArrayList<>();

    private FeatureManager() {
        // Initialize category lists
        for (FeatureCategory category : FeatureCategory.values()) {
            featuresByCategory.put(category, new CopyOnWriteArrayList<>());
        }

        // Register event handlers
        registerSystemEventHandlers();
    }

    public static FeatureManager getInstance() {
        return INSTANCE;
    }

    /**
     * Register a new feature with dependency validation
     */
    public void registerFeature(Feature feature) {
        if (features.containsKey(feature.getId())) {
            LOGGER.warn("Feature with ID '{}' is already registered, skipping", feature.getId());
            return;
        }

        // Validate feature requirements
        if (!validateFeatureRequirements(feature)) {
            LOGGER.warn("Feature '{}' does not meet minimum requirements, skipping registration", feature.getId());
            return;
        }

        // Check for conflicts
        List<String> conflicts = checkForConflicts(feature);
        if (!conflicts.isEmpty()) {
            LOGGER.warn("Feature '{}' has conflicts with existing features: {}", feature.getId(), conflicts);
            // Still register but log the warning
        }

        features.put(feature.getId(), feature);
        featuresByCategory.get(feature.getCategory()).add(feature);

        LOGGER.debug("Registered feature: {} v{} by {} (category: {})",
                    feature.getName(), feature.getVersion(), feature.getAuthor(), feature.getCategory());

        // Post feature registered event
        eventBus.postFeatureEvent(FeatureEventType.FEATURE_INITIALIZED.getEventName(), feature.getId(),
                                 Map.of("action", "registered", "version", feature.getVersion()));
    }

    /**
     * Validate that a feature meets minimum requirements
     */
    private boolean validateFeatureRequirements(Feature feature) {
        // Check minimum Minecraft version
        String currentVersion = com.criticalrange.util.VersionHelper.getCurrentVersion();
        String minVersion = feature.getMinimumMinecraftVersion();

        // Simple version comparison (can be enhanced with proper version parsing)
        if (currentVersion.compareTo(minVersion) < 0) {
            LOGGER.warn("Feature '{}' requires Minecraft {} or higher, current version is {}",
                       feature.getId(), minVersion, currentVersion);
            return false;
        }

        return true;
    }

    /**
     * Check for conflicts with existing features
     */
    private List<String> checkForConflicts(Feature feature) {
        List<String> conflicts = new ArrayList<>();

        for (Feature existing : features.values()) {
            if (existing.hasConflictWith(feature.getId()) || feature.hasConflictWith(existing.getId())) {
                conflicts.add(existing.getId());
            }
        }

        // Check dependency graph for conflicts
        List<Feature> existingFeatures = features.values().stream()
                .filter(f -> !f.getId().equals(feature.getId()))
                .collect(Collectors.toList());

        DependencyGraph tempGraph = DependencyGraph.createValidationInstance();
        for (Feature existing : existingFeatures) {
            existing.getDependencies().forEach(tempGraph::addDependency);
        }
        feature.getDependencies().forEach(tempGraph::addDependency);

        DependencyGraph.DependencyValidationResult validationResult =
            tempGraph.validateDependencies(new HashSet<>(features.keySet()) {{
                add(feature.getId());
            }});

        conflicts.addAll(validationResult.conflicts());

        return conflicts;
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
        return new ArrayList<>(features.values());
    }

    /**
     * Get features by category
     */
    public List<Feature> getFeaturesByCategory(FeatureCategory category) {
        return new ArrayList<>(featuresByCategory.getOrDefault(category, new ArrayList<>()));
    }

    /**
     * Get all categories that have features
     */
    public Set<FeatureCategory> getActiveCategories() {
        return featuresByCategory.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Initialize all registered features with dependency resolution
     */
    public void initializeFeatures(MinecraftClient minecraft) {
        if (initialized) {
            LOGGER.warn("Features already initialized, skipping");
            return;
        }

        LOGGER.debug("Initializing {} registered features...", features.size());

        // Validate all dependencies
        DependencyGraph.DependencyValidationResult validationResult =
            dependencyGraph.validateDependencies(features.keySet());

        if (!validationResult.isValid()) {
            LOGGER.error("Dependency validation failed:\n{}", validationResult.getSummary());
            // Continue with initialization but log the issues
        }

        if (validationResult.hasWarnings()) {
            LOGGER.warn("Dependency warnings:\n{}", String.join("\n", validationResult.warnings()));
        }

        // Resolve loading order
        loadingOrder = dependencyGraph.resolveLoadingOrder(features.keySet());
        LOGGER.debug("Feature loading order: {}", loadingOrder);

        // Initialize features in dependency order
        int successCount = 0;
        int failureCount = 0;

        for (String featureId : loadingOrder) {
            Feature feature = features.get(featureId);
            if (feature != null) {
                try {
                    feature.initialize(minecraft);
                    LOGGER.debug("Initialized feature: {}", feature.getName());
                    successCount++;
                } catch (Exception e) {
                    LOGGER.error("Failed to initialize feature: {}", feature.getName(), e);
                    failureCount++;

                    // Attempt recovery
                    errorRecoveryManager.handleError(featureId, "initialization", e,
                        com.criticalrange.core.error.ErrorSeverity.CRITICAL);
                }
            }
        }

        initialized = true;
        LOGGER.debug("Feature initialization complete: {} successful, {} failed", successCount, failureCount);

        // Post system initialization event
        eventBus.post(FeatureEventType.FEATURE_INITIALIZED.getEventName(),
                     Map.of("totalFeatures", features.size(), "successCount", successCount, "failureCount", failureCount));
    }

    /**
     * Tick all enabled features with error handling
     */
    public void tickFeatures(MinecraftClient minecraft) {
        if (!initialized) {
            return;
        }

        // Post tick event
        eventBus.post(FeatureEventType.RENDER_TICK.getEventName(),
                     Map.of("minecraft", minecraft, "timestamp", System.currentTimeMillis()));

        for (Feature feature : features.values()) {
            if (feature.isEnabled()) {
                errorRecoveryManager.handleError(feature.getId(), "tick", () -> {
                    feature.onTick(minecraft);
                }, com.criticalrange.core.error.ErrorSeverity.ERROR);
            }
        }
    }

    /**
     * Enable a feature by ID with dependency checks
     */
    public boolean enableFeature(String id) {
        return enableFeature(id, false);
    }

    /**
     * Enable a feature by ID with optional force flag
     */
    public boolean enableFeature(String id, boolean force) {
        Optional<Feature> featureOpt = getFeature(id);
        if (featureOpt.isPresent()) {
            Feature feature = featureOpt.get();
            if (!feature.isEnabled()) {
                // Check dependencies
                if (!force) {
                    List<String> missingDeps = getMissingDependencies(id);
                    if (!missingDeps.isEmpty()) {
                        LOGGER.warn("Cannot enable feature '{}' - missing dependencies: {}", id, missingDeps);
                        return false;
                    }
                }

                return errorRecoveryManager.handleError(id, "enable_feature", () -> {
                    feature.setEnabled(true);
                    feature.onEnable();
                    LOGGER.info("Enabled feature: {}", feature.getName());
                    return true;
                }, false, com.criticalrange.core.error.ErrorSeverity.ERROR);
            }
        }
        return false;
    }

    /**
     * Disable a feature by ID with safety checks
     */
    public boolean disableFeature(String id) {
        return disableFeature(id, false);
    }

    /**
     * Disable a feature by ID with optional force flag
     */
    public boolean disableFeature(String id, boolean force) {
        Optional<Feature> featureOpt = getFeature(id);
        if (featureOpt.isPresent()) {
            Feature feature = featureOpt.get();
            if (feature.isEnabled()) {
                // Check if feature can be safely disabled
                if (!force && !feature.canSafelyDisable()) {
                    LOGGER.warn("Feature '{}' cannot be safely disabled", id);
                    return false;
                }

                // Check if other features depend on this one
                List<String> dependents = getDependentFeatures(id);
                if (!force && !dependents.isEmpty()) {
                    LOGGER.warn("Cannot disable feature '{}' - depended upon by: {}", id, dependents);
                    return false;
                }

                return errorRecoveryManager.handleError(id, "disable_feature", () -> {
                    feature.onDisable();
                    feature.setEnabled(false);
                    LOGGER.info("Disabled feature: {}", feature.getName());
                    return true;
                }, false, com.criticalrange.core.error.ErrorSeverity.ERROR);
            }
        }
        return false;
    }

    /**
     * Get missing dependencies for a feature
     */
    private List<String> getMissingDependencies(String featureId) {
        List<String> missing = new ArrayList<>();
        Feature feature = features.get(featureId);

        if (feature != null) {
            for (FeatureDependency dependency : feature.getDependencies()) {
                if (dependency.getType() == DependencyType.REQUIRED &&
                    !features.containsKey(dependency.getRequiredFeature())) {
                    missing.add(dependency.getRequiredFeature());
                }
            }
        }

        return missing;
    }

    /**
     * Get features that depend on the given feature
     */
    private List<String> getDependentFeatures(String featureId) {
        return dependencyGraph.getDependentsOfFeature(featureId).stream()
                .map(FeatureDependency::getDependentFeature)
                .filter(features::containsKey)
                .collect(Collectors.toList());
    }

    /**
     * Perform health check on all features
     */
    public FeatureHealthSummary performHealthChecks() {
        Map<String, Boolean> healthResults = new HashMap<>();
        int healthyCount = 0;
        int unhealthyCount = 0;

        for (Feature feature : features.values()) {
            boolean healthy = errorRecoveryManager.handleError(feature.getId(), "health_check",
                () -> feature.performHealthCheck(), false);
            healthResults.put(feature.getId(), healthy);

            if (healthy) {
                healthyCount++;
            } else {
                unhealthyCount++;
            }
        }

        return new FeatureHealthSummary(healthResults, healthyCount, unhealthyCount);
    }

    /**
     * Get system diagnostic information
     */
    public String getSystemDiagnostics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== VulkanMod Extra Feature System Diagnostics ===\n");
        sb.append(String.format("Total Features: %d\n", features.size()));
        sb.append(String.format("Enabled Features: %d\n", getEnabledFeatureCount()));
        sb.append(String.format("Initialized: %b\n", initialized));

        // Event bus statistics
        EventBus.EventStatistics eventStats = eventBus.getStatistics();
        sb.append(String.format("Events Processed: %d\n", eventStats.eventsProcessed()));
        sb.append(String.format("Events Failed: %d\n", eventStats.eventsFailed()));
        sb.append(String.format("Event Success Rate: %.1f%%\n", eventStats.getSuccessRate()));

        // Error recovery statistics
        ErrorRecoveryManager.ErrorStatistics errorStats = errorRecoveryManager.getErrorStatistics();
        sb.append(String.format("Total Errors: %d\n", errorStats.totalErrors()));
        sb.append(String.format("Recovered Errors: %d\n", errorStats.recoveredErrors()));
        sb.append(String.format("Recovery Rate: %.1f%%\n", errorStats.getRecoveryRate()));

        // Dependency graph information
        sb.append(String.format("Total Dependencies: %d\n", dependencyGraph.getAllDependencies().size()));

        // Health check summary
        FeatureHealthSummary healthSummary = performHealthChecks();
        sb.append(String.format("Healthy Features: %d\n", healthSummary.healthyCount()));
        sb.append(String.format("Unhealthy Features: %d\n", healthSummary.unhealthyCount()));

        return sb.toString();
    }

    /**
     * Register system-level event handlers
     */
    private void registerSystemEventHandlers() {
        // Handle configuration changes
        eventBus.register(FeatureEventType.CONFIG_CHANGED.getEventName(), event -> {
            String featureId = event.getData("featureId", String.class);
            if (featureId != null) {
                Feature feature = features.get(featureId);
                if (feature != null) {
                    String configKey = event.getData("configKey", String.class);
                    Object oldValue = event.getData("oldValue", Object.class);
                    Object newValue = event.getData("newValue", Object.class);
                    feature.onConfigChange(configKey, oldValue, newValue);
                }
            }
            return false; // Don't stop propagation
        }, 10);

        // Handle resource reloads
        eventBus.register(FeatureEventType.RELOAD_RESOURCES.getEventName(), event -> {
            for (Feature feature : features.values()) {
                errorRecoveryManager.handleError(feature.getId(), "resource_reload",
                    () -> feature.onResourceReload(), com.criticalrange.core.error.ErrorSeverity.WARNING);
            }
            return false; // Don't stop propagation
        }, 5);
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

    /**
     * Get the current loading order
     */
    public List<String> getLoadingOrder() {
        return new ArrayList<>(loadingOrder);
    }

    /**
     * Health summary record
     */
    public record FeatureHealthSummary(Map<String, Boolean> healthResults, int healthyCount, int unhealthyCount) {}
}
