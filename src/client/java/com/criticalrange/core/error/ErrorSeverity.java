package com.criticalrange.core.error;

/**
 * Severity levels for feature errors
 */
public enum ErrorSeverity {
    /**
     * Informational messages that don't affect functionality
     */
    INFO("info", "Informational message", 1),

    /**
     * Warnings that don't prevent feature operation but should be addressed
     */
    WARNING("warning", "Warning message", 2),

    /**
     * Errors that affect feature operation but are recoverable
     */
    ERROR("error", "Recoverable error", 3),

    /**
     * Critical errors that may prevent the feature from functioning
     */
    CRITICAL("critical", "Critical error", 4),

    /**
     * Fatal errors that require immediate attention
     */
    FATAL("fatal", "Fatal error", 5);

    private final String severityName;
    private final String description;
    private final int level;

    ErrorSeverity(String severityName, String description, int level) {
        this.severityName = severityName;
        this.description = description;
        this.level = level;
    }

    public String getSeverityName() {
        return severityName;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public boolean isAtLeast(ErrorSeverity other) {
        return this.level >= other.level;
    }

    public boolean isWorseThan(ErrorSeverity other) {
        return this.level > other.level;
    }

    public static ErrorSeverity fromLevel(int level) {
        for (ErrorSeverity severity : values()) {
            if (severity.level == level) {
                return severity;
            }
        }
        return ERROR; // Default fallback
    }
}