package com.criticalrange.core.error;

import java.time.Instant;

/**
 * Represents an error that occurred in a feature
 */
public class FeatureError {
    private final String featureId;
    private final String errorType;
    private final String message;
    private final Throwable cause;
    private final Instant timestamp;
    private final ErrorSeverity severity;
    private int retryCount;
    private boolean recovered;

    public FeatureError(String featureId, String errorType, String message, Throwable cause, ErrorSeverity severity) {
        this.featureId = featureId;
        this.errorType = errorType;
        this.message = message;
        this.cause = cause;
        this.severity = severity;
        this.timestamp = Instant.now();
        this.retryCount = 0;
        this.recovered = false;
    }

    public String getFeatureId() {
        return featureId;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public boolean isRecovered() {
        return recovered;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void setRecovered(boolean recovered) {
        this.recovered = recovered;
    }

    public String getFullMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Feature Error: ").append(featureId)
          .append(" [").append(errorType).append("]")
          .append(" - ").append(message)
          .append(" (Severity: ").append(severity).append(")");

        if (cause != null) {
            sb.append("\nCaused by: ").append(cause.getClass().getSimpleName())
              .append(": ").append(cause.getMessage());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("FeatureError[feature=%s, type=%s, severity=%s, message=%s, retries=%d, recovered=%b]",
                           featureId, errorType, severity, message, retryCount, recovered);
    }
}