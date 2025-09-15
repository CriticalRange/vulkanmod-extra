package com.criticalrange.core;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.core.dependency.DependencyGraph;
import com.criticalrange.core.dependency.FeatureDependency;
import com.criticalrange.core.dependency.DependencyType;
import com.criticalrange.core.events.EventBus;
import com.criticalrange.core.events.FeatureEvent;
import com.criticalrange.core.events.FeatureEventType;
import com.criticalrange.core.events.FeatureEventHandler;
import com.criticalrange.core.error.ErrorRecoveryManager;
import com.criticalrange.core.error.ErrorRecoveryStrategy;
import com.criticalrange.core.error.ErrorSeverity;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enhanced abstract base class for features with dependency management, event handling, and error recovery
 */
public abstract class BaseFeature implements Feature {
    protected final String id;
    protected final String name;
    protected final FeatureCategory category;
    protected boolean enabled = true; // Default to enabled for compatibility
    protected String description = "No description available";
    protected String version = "1.0.0";
    protected String author = "VulkanMod Extra";
    protected ErrorRecoveryStrategy recoveryStrategy = ErrorRecoveryStrategy.RETRY_DELAYED;
    protected int eventPriority = 0;

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean eventHandlersRegistered = new AtomicBoolean(false);

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
    public String getVersion() {
        return version;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean wasEnabled = this.enabled;
        this.enabled = enabled;

        if (wasEnabled != enabled) {
            if (enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    @Override
    public FeatureCategory getCategory() {
        return category;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<FeatureDependency> getDependencies() {
        return List.of(); // Override in subclasses to add dependencies
    }

    @Override
    public ErrorRecoveryStrategy getRecoveryStrategy() {
        return recoveryStrategy;
    }

    @Override
    public int getEventPriority() {
        return eventPriority;
    }

    @Override
    public void initialize(MinecraftClient minecraft) {
        if (initialized.compareAndSet(false, true)) {
            // Set up error recovery
            ErrorRecoveryManager.getInstance().setRecoveryStrategy(getId(), getRecoveryStrategy());

            // Register event handlers
            if (eventHandlersRegistered.compareAndSet(false, true)) {
                registerEventHandlers();
                registerFeatureEventHandler();
            }

            // Register dependencies
            registerDependencies();

            // Initialize feature-specific logic
            doInitialize(minecraft);

            // Post initialization event
            postFeatureInitializedEvent();
        }
    }

    /**
     * Override this method for feature-specific initialization
     */
    protected void doInitialize(MinecraftClient minecraft) {
        // Default implementation - override if needed
    }

    /**
     * Register this feature's dependencies with the dependency graph
     */
    protected void registerDependencies() {
        List<FeatureDependency> dependencies = getDependencies();
        if (!dependencies.isEmpty()) {
            DependencyGraph graph = DependencyGraph.getInstance();
            for (FeatureDependency dependency : dependencies) {
                graph.addDependency(dependency);
            }
        }
    }

    /**
     * Register the main feature event handler
     */
    protected void registerFeatureEventHandler() {
        EventBus.getInstance().register("*", this::handleEvent, getEventPriority());
    }

    @Override
    public void registerEventHandlers() {
        // Override in subclasses to register specific event handlers
    }

    /**
     * Post feature initialization event
     */
    protected void postFeatureInitializedEvent() {
        EventBus.getInstance().postFeatureEvent(FeatureEventType.FEATURE_INITIALIZED.getEventName(), getId(),
            Map.of("version", version, "author", author, "category", category.name()));
    }

    @Override
    public void onTick(MinecraftClient minecraft) {
        handleError("tick", () -> {
            doTick(minecraft);
        });
    }

    /**
     * Override this method for feature-specific ticking logic
     */
    protected void doTick(MinecraftClient minecraft) {
        // Default implementation - override if needed
    }

    @Override
    public boolean performHealthCheck() {
        return handleError("health_check", () -> {
            return isEnabled() && this.initialized.get();
        }, false);
    }

    @Override
    public String getDiagnosticInfo() {
        return String.format("Feature: %s v%s by %s (category: %s, enabled: %b, initialized: %b)",
                           name, version, author, category.name(), enabled, this.initialized.get());
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
        return handleError("get_minecraft", () -> {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            if (minecraft == null) {
                getLogger().warn("MinecraftClient instance is null!");
            }
            return minecraft;
        }, null);
    }

    /**
     * Get the main config instance with error handling
     */
    protected com.criticalrange.config.VulkanModExtraConfig getConfig() {
        return handleError("get_config", () -> {
            return VulkanModExtra.CONFIG;
        }, null);
    }

    /**
     * Mark config as changed (will be saved) with error handling
     */
    protected void markConfigChanged() {
        handleError("mark_config_changed", () -> {
            if (VulkanModExtra.CONFIG != null) {
                VulkanModExtra.CONFIG.writeChanges();
            }
        }, ErrorSeverity.WARNING);
    }

    /**
     * Add a dependency for this feature
     */
    protected void addDependency(String requiredFeature, DependencyType type, String description) {
        addDependency(requiredFeature, type, description, false);
    }

    /**
     * Add a dependency for this feature
     */
    protected void addDependency(String requiredFeature, DependencyType type, String description, boolean optional) {
        FeatureDependency dependency = new FeatureDependency(getId(), requiredFeature, type, description, optional);
        // This will be processed during registration
    }

    /**
     * Post a configuration change event
     */
    protected void postConfigChangeEvent(String configKey, Object oldValue, Object newValue) {
        EventBus.getInstance().postFeatureEvent(FeatureEventType.CONFIG_CHANGED.getEventName(), getId(),
            Map.of("configKey", configKey, "oldValue", oldValue, "newValue", newValue));
    }

    /**
     * Post a performance warning event
     */
    protected void postPerformanceWarning(String warning, Object data) {
        EventBus.getInstance().postFeatureEvent(FeatureEventType.PERFORMANCE_WARNING.getEventName(), getId(),
            Map.of("warning", warning, "data", data));
    }

    /**
     * Safe feature operation with error handling
     */
    protected void safeExecute(String operationName, Runnable operation) {
        handleError(operationName, operation, ErrorSeverity.ERROR);
    }

    /**
     * Safe feature operation with error handling and return value
     */
    protected <T> T safeExecute(String operationName, java.util.function.Supplier<T> supplier, T defaultValue) {
        return handleError(operationName, supplier, defaultValue);
    }

    /**
     * Get the initialized status for subclasses
     */
    protected java.util.concurrent.atomic.AtomicBoolean getInitialized() {
        return initialized;
    }
}
