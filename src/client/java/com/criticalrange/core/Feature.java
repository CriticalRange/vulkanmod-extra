package com.criticalrange.core;

import com.criticalrange.core.dependency.FeatureDependency;
import com.criticalrange.core.events.EventBus;
import com.criticalrange.core.events.FeatureEvent;
import com.criticalrange.core.events.FeatureEventType;
import com.criticalrange.core.error.ErrorRecoveryManager;
import com.criticalrange.core.error.ErrorRecoveryStrategy;
import com.criticalrange.core.error.ErrorSeverity;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.Map;

/**
 * Enhanced interface for all VulkanMod Extra features.
 * Provides dependency management, event-driven architecture, and error recovery.
 */
public interface Feature {

    /**
     * Get the unique identifier for this feature
     */
    String getId();

    /**
     * Get the display name for this feature
     */
    String getName();

    /**
     * Get the version of this feature
     */
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * Get the author of this feature
     */
    default String getAuthor() {
        return "VulkanMod Extra";
    }

    /**
     * Get the minimum required Minecraft version
     */
    default String getMinimumMinecraftVersion() {
        return "1.21.1";
    }

    /**
     * Get the dependencies for this feature
     */
    default List<FeatureDependency> getDependencies() {
        return List.of();
    }

    /**
     * Get the error recovery strategy for this feature
     */
    default ErrorRecoveryStrategy getRecoveryStrategy() {
        return ErrorRecoveryStrategy.RETRY_DELAYED;
    }

    /**
     * Get the priority for event handling (higher = handled first)
     */
    default int getEventPriority() {
        return 0;
    }

    /**
     * Initialize the feature. Called once during client initialization.
     */
    default void initialize(MinecraftClient minecraft) {
        // Register event handlers
        registerEventHandlers();

        // Set up error recovery
        ErrorRecoveryManager.getInstance().setRecoveryStrategy(getId(), getRecoveryStrategy());

        // Post initialization event
        EventBus.getInstance().postFeatureEvent(FeatureEventType.FEATURE_INITIALIZED.getEventName(), getId(), null);
    }

    /**
     * Register event handlers for this feature
     */
    default void registerEventHandlers() {
        // Override to register specific event handlers
    }

    /**
     * Check if this feature is enabled
     */
    boolean isEnabled();

    /**
     * Enable or disable this feature
     */
    void setEnabled(boolean enabled);

    /**
     * Called when the feature is enabled
     */
    default void onEnable() {
        EventBus.getInstance().postFeatureEvent(FeatureEventType.FEATURE_ENABLED.getEventName(), getId(), null);
    }

    /**
     * Called when the feature is disabled
     */
    default void onDisable() {
        EventBus.getInstance().postFeatureEvent(FeatureEventType.FEATURE_DISABLED.getEventName(), getId(), null);
    }

    /**
     * Called every client tick
     */
    default void onTick(MinecraftClient minecraft) {
        // Default implementation - override if needed
    }

    /**
     * Called when a configuration change occurs
     */
    default void onConfigChange(String configKey, Object oldValue, Object newValue) {
        // Default implementation - override if needed
    }

    /**
     * Called when resources are reloaded
     */
    default void onResourceReload() {
        // Default implementation - override if needed
    }

    /**
     * Called when the world is loaded
     */
    default void onWorldLoad(MinecraftClient minecraft) {
        // Default implementation - override if needed
    }

    /**
     * Called when the world is unloaded
     */
    default void onWorldUnload(MinecraftClient minecraft) {
        // Default implementation - override if needed
    }

    /**
     * Handle a feature event
     */
    default boolean handleEvent(FeatureEvent event) {
        // Default implementation - override to handle specific events
        return false;
    }

    /**
     * Check if this feature can safely be disabled
     */
    default boolean canSafelyDisable() {
        return true;
    }

    /**
     * Check if this feature has any conflicts with the given feature
     */
    default boolean hasConflictWith(String otherFeatureId) {
        return false;
    }

    /**
     * Get diagnostic information for this feature
     */
    default String getDiagnosticInfo() {
        return String.format("Feature: %s v%s (enabled: %b)", getName(), getVersion(), isEnabled());
    }

    /**
     * Perform health check on this feature
     */
    default boolean performHealthCheck() {
        return isEnabled(); // Default health check
    }

    /**
     * Get the category this feature belongs to
     */
    FeatureCategory getCategory();

    /**
     * Get a description of what this feature does
     */
    default String getDescription() {
        return "No description available";
    }

    /**
     * Post an event to the event bus
     */
    default void postEvent(FeatureEvent event) {
        EventBus.getInstance().post(event);
    }

    /**
     * Post a simple event
     */
    default void postEvent(String eventType) {
        EventBus.getInstance().post(eventType);
    }

    /**
     * Post an event with data
     */
    default void postEvent(String eventType, Object data) {
        EventBus.getInstance().post(eventType, Map.of("data", data, "featureId", getId()));
    }

    /**
     * Handle an error with recovery
     */
    default <T> T handleError(String operation, java.util.function.Supplier<T> operationSupplier, T fallbackValue) {
        return ErrorRecoveryManager.getInstance().handleError(getId(), operation, operationSupplier, fallbackValue);
    }

    /**
     * Handle an error with custom severity
     */
    default <T> T handleError(String operation, java.util.function.Supplier<T> operationSupplier, T fallbackValue, ErrorSeverity severity) {
        return ErrorRecoveryManager.getInstance().handleError(getId(), operation, operationSupplier, fallbackValue, severity);
    }

    /**
     * Handle an error (void version)
     */
    default void handleError(String operationName, Runnable operation) {
        ErrorRecoveryManager.getInstance().handleError(getId(), operationName, operation);
    }

    /**
     * Handle an error with custom severity (void version)
     */
    default void handleError(String operationName, Runnable operation, ErrorSeverity severity) {
        ErrorRecoveryManager.getInstance().handleError(getId(), operationName, operation, severity);
    }
}
