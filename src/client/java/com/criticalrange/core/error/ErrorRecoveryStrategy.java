package com.criticalrange.core.error;

/**
 * Strategy for recovering from feature errors
 */
public enum ErrorRecoveryStrategy {
    /**
     * Immediately retry the failed operation
     */
    RETRY("retry", "Retry the failed operation immediately"),

    /**
     * Retry after a delay
     */
    RETRY_DELAYED("retry_delayed", "Retry the failed operation after a delay"),

    /**
     * Skip the current operation but continue running
     */
    SKIP("skip", "Skip the failed operation and continue"),

    /**
     * Disable the feature that caused the error
     */
    DISABLE_FEATURE("disable_feature", "Disable the feature that caused the error"),

    /**
     * Use fallback/default behavior
     */
    FALLBACK("fallback", "Use fallback or default behavior"),

    /**
     * Log the error but continue normally
     */
    LOG_ONLY("log_only", "Log the error and continue normally"),

    /**
     * Gracefully shut down the feature
     */
    GRACEFUL_SHUTDOWN("graceful_shutdown", "Perform a graceful shutdown of the feature"),

    /**
     * No recovery - fail fast
     */
    FAIL_FAST("fail_fast", "Fail immediately without recovery");

    private final String strategyName;
    private final String description;

    ErrorRecoveryStrategy(String strategyName, String description) {
        this.strategyName = strategyName;
        this.description = description;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public String getDescription() {
        return description;
    }

    public boolean shouldRetry() {
        return this == RETRY || this == RETRY_DELAYED;
    }

    public boolean shouldDisableFeature() {
        return this == DISABLE_FEATURE;
    }

    public boolean shouldContinue() {
        return this != FAIL_FAST;
    }
}