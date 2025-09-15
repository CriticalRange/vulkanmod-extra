package com.criticalrange.core.error;

import com.criticalrange.core.events.EventBus;
import com.criticalrange.core.events.FeatureEvent;
import com.criticalrange.core.events.FeatureEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Manages error recovery for all features
 * Provides automatic retry, fallback mechanisms, and graceful degradation
 */
public class ErrorRecoveryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra Error Recovery");
    private static final ErrorRecoveryManager INSTANCE = new ErrorRecoveryManager();

    private final Map<String, List<FeatureError>> errorHistory = new ConcurrentHashMap<>();
    private final Map<String, ErrorRecoveryStrategy> featureStrategies = new ConcurrentHashMap<>();
    private final Map<String, Integer> retryCounts = new ConcurrentHashMap<>();
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(2);

    // Configuration
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final int MAX_ERROR_HISTORY_SIZE = 100;

    private ErrorRecoveryManager() {
        // Private constructor for singleton
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public static ErrorRecoveryManager getInstance() {
        return INSTANCE;
    }

    /**
     * Set the recovery strategy for a feature
     */
    public void setRecoveryStrategy(String featureId, ErrorRecoveryStrategy strategy) {
        featureStrategies.put(featureId, strategy);
        LOGGER.debug("Set recovery strategy for feature '{}' to '{}'", featureId, strategy.getStrategyName());
    }

    /**
     * Handle an error in a feature
     */
    public <T> T handleError(String featureId, String operation, Supplier<T> operationSupplier, T fallbackValue) {
        return handleError(featureId, operation, operationSupplier, fallbackValue, ErrorSeverity.ERROR);
    }

    /**
     * Handle an error in a feature with custom severity
     */
    public <T> T handleError(String featureId, String operation, Supplier<T> operationSupplier,
                           T fallbackValue, ErrorSeverity severity) {
        try {
            return operationSupplier.get();
        } catch (Exception e) {
            handleError(featureId, operation, e, severity);
            return fallbackValue;
        }
    }

    /**
     * Handle an error in a feature (void version)
     */
    public void handleError(String featureId, String operationName, Runnable operation) {
        handleError(featureId, operationName, operation, ErrorSeverity.ERROR);
    }

    /**
     * Handle an error in a feature with custom severity (void version)
     */
    public void handleError(String featureId, String operationName, Runnable operation, ErrorSeverity severity) {
        try {
            operation.run();
        } catch (Exception e) {
            handleError(featureId, operationName, e, severity);
        }
    }

    /**
     * Handle an exception that occurred in a feature
     */
    public void handleError(String featureId, String operation, Throwable cause, ErrorSeverity severity) {
        FeatureError error = new FeatureError(featureId, operation, cause.getMessage(), cause, severity);
        recordError(error);

        ErrorRecoveryStrategy strategy = featureStrategies.getOrDefault(featureId, getDefaultStrategy(severity));

        try {
            boolean recovered = attemptRecovery(featureId, error, strategy);

            if (recovered) {
                error.setRecovered(true);
                EventBus.getInstance().postFeatureEvent(FeatureEventType.FEATURE_RECOVERED.getEventName(),
                                                       featureId, Map.of("error", error, "strategy", strategy));
                LOGGER.info("Feature '{}' recovered from error: {}", featureId, operation);
            } else {
                EventBus.getInstance().postFeatureEvent(FeatureEventType.FEATURE_ERROR.getEventName(),
                                                       featureId, Map.of("error", error, "strategy", strategy));
            }
        } catch (Exception recoveryException) {
            LOGGER.error("Recovery attempt failed for feature '{}': {}", featureId, recoveryException.getMessage(), recoveryException);
        }
    }

    /**
     * Get the default recovery strategy based on error severity
     */
    private ErrorRecoveryStrategy getDefaultStrategy(ErrorSeverity severity) {
        return switch (severity) {
            case INFO, WARNING -> ErrorRecoveryStrategy.LOG_ONLY;
            case ERROR -> ErrorRecoveryStrategy.RETRY_DELAYED;
            case CRITICAL -> ErrorRecoveryStrategy.DISABLE_FEATURE;
            case FATAL -> ErrorRecoveryStrategy.FAIL_FAST;
        };
    }

    /**
     * Attempt to recover from an error using the specified strategy
     */
    private boolean attemptRecovery(String featureId, FeatureError error, ErrorRecoveryStrategy strategy) {
        switch (strategy) {
            case RETRY:
                return attemptImmediateRetry(featureId, error);

            case RETRY_DELAYED:
                return attemptDelayedRetry(featureId, error);

            case DISABLE_FEATURE:
                return attemptFeatureDisable(featureId, error);

            case FALLBACK:
                return attemptFallbackRecovery(featureId, error);

            case GRACEFUL_SHUTDOWN:
                return attemptGracefulShutdown(featureId, error);

            case SKIP:
            case LOG_ONLY:
                return true; // Considered "recovered" by continuing

            case FAIL_FAST:
                return false; // No recovery attempted

            default:
                LOGGER.warn("Unknown recovery strategy: {}", strategy);
                return false;
        }
    }

    /**
     * Attempt immediate retry
     */
    private boolean attemptImmediateRetry(String featureId, FeatureError error) {
        int retryCount = retryCounts.getOrDefault(featureId, 0);
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            error.incrementRetryCount();
            retryCounts.put(featureId, retryCount + 1);
            LOGGER.debug("Retrying feature '{}' operation '{}' (attempt {})", featureId, error.getErrorType(), retryCount + 1);
            return true; // Caller should retry the operation
        }
        return false;
    }

    /**
     * Attempt delayed retry
     */
    private boolean attemptDelayedRetry(String featureId, FeatureError error) {
        int retryCount = retryCounts.getOrDefault(featureId, 0);
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            error.incrementRetryCount();
            retryCounts.put(featureId, retryCount + 1);

            retryExecutor.schedule(() -> {
                LOGGER.debug("Executing delayed retry for feature '{}' operation '{}' (attempt {})",
                           featureId, error.getErrorType(), retryCount + 1);
                EventBus.getInstance().postFeatureEvent(FeatureEventType.FEATURE_RECOVERED.getEventName(),
                                                       featureId, Map.of("retry", true));
            }, RETRY_DELAY_MS, TimeUnit.MILLISECONDS);

            return true;
        }
        return false;
    }

    /**
     * Attempt to disable the feature
     */
    private boolean attemptFeatureDisable(String featureId, FeatureError error) {
        try {
            EventBus.getInstance().postFeatureEvent(FeatureEventType.FEATURE_DISABLED.getEventName(),
                                                   featureId, Map.of("reason", "error_recovery", "error", error));
            LOGGER.warn("Feature '{}' disabled due to error: {}", featureId, error.getMessage());
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to disable feature '{}': {}", featureId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Attempt fallback recovery
     */
    private boolean attemptFallbackRecovery(String featureId, FeatureError error) {
        LOGGER.info("Using fallback behavior for feature '{}' after error: {}", featureId, error.getMessage());
        return true; // Feature should use fallback logic
    }

    /**
     * Attempt graceful shutdown
     */
    private boolean attemptGracefulShutdown(String featureId, FeatureError error) {
        try {
            EventBus.getInstance().postFeatureEvent(FeatureEventType.FEATURE_DISABLED.getEventName(),
                                                   featureId, Map.of("reason", "graceful_shutdown", "error", error));
            LOGGER.info("Feature '{}' gracefully shut down due to error: {}", featureId, error.getMessage());
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to gracefully shutdown feature '{}': {}", featureId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Record an error in the history
     */
    private void recordError(FeatureError error) {
        errorHistory.computeIfAbsent(error.getFeatureId(), k -> new CopyOnWriteArrayList<>())
                   .add(error);

        // Trim history if needed
        List<FeatureError> featureErrors = errorHistory.get(error.getFeatureId());
        if (featureErrors.size() > MAX_ERROR_HISTORY_SIZE) {
            featureErrors.remove(0);
        }

        LOGGER.error("Error recorded for feature '{}': {} - {}",
                    error.getFeatureId(), error.getErrorType(), error.getMessage());
    }

    /**
     * Get error history for a feature
     */
    public List<FeatureError> getErrorHistory(String featureId) {
        return new ArrayList<>(errorHistory.getOrDefault(featureId, Collections.emptyList()));
    }

    /**
     * Get all error history
     */
    public Map<String, List<FeatureError>> getAllErrorHistory() {
        Map<String, List<FeatureError>> copy = new HashMap<>();
        errorHistory.forEach((feature, errors) -> copy.put(feature, new ArrayList<>(errors)));
        return copy;
    }

    /**
     * Clear error history for a feature
     */
    public void clearErrorHistory(String featureId) {
        errorHistory.remove(featureId);
        retryCounts.remove(featureId);
    }

    /**
     * Clear all error history
     */
    public void clearAllErrorHistory() {
        errorHistory.clear();
        retryCounts.clear();
    }

    /**
     * Get error statistics
     */
    public ErrorStatistics getErrorStatistics() {
        long totalErrors = errorHistory.values().stream().mapToLong(List::size).sum();
        long recoveredErrors = errorHistory.values().stream()
                                          .flatMap(List::stream)
                                          .filter(FeatureError::isRecovered)
                                          .count();

        return new ErrorStatistics(totalErrors, recoveredErrors, featureStrategies.size());
    }

    /**
     * Shutdown the recovery manager
     */
    private void shutdown() {
        retryExecutor.shutdown();
        try {
            if (!retryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                retryExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            retryExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Error statistics record
     */
    public record ErrorStatistics(long totalErrors, long recoveredErrors, int configuredStrategies) {
        public double getRecoveryRate() {
            return totalErrors > 0 ? recoveredErrors * 100.0 / totalErrors : 100.0;
        }
    }
}