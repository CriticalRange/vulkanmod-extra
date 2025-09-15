package com.criticalrange.core.dependency;

/**
 * Types of dependencies between features
 */
public enum DependencyType {
    /**
     * The dependent feature requires the required feature to function at all
     */
    REQUIRED("required", "Required dependency - feature cannot function without this"),

    /**
     * The dependent feature works better with the required feature, but can function without it
     */
    RECOMMENDED("recommended", "Recommended dependency - feature works better with this"),

    /**
     * The dependent feature should be enabled after the required feature
     */
    LOAD_AFTER("load_after", "Load order dependency - should load after this feature"),

    /**
     * The dependent feature should be enabled before the required feature
     */
    LOAD_BEFORE("load_before", "Load order dependency - should load before this feature"),

    /**
     * The features conflict and should not be enabled simultaneously
     */
    CONFLICT("conflict", "Conflict dependency - features cannot be enabled together"),

    /**
     * The dependent feature enhances the required feature
     */
    ENHANCES("enhances", "Enhancement dependency - feature enhances the required feature");

    private final String typeName;
    private final String description;

    DependencyType(String typeName, String description) {
        this.typeName = typeName;
        this.description = description;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return this == REQUIRED;
    }

    public boolean isConflict() {
        return this == CONFLICT;
    }

    public boolean isLoadOrder() {
        return this == LOAD_AFTER || this == LOAD_BEFORE;
    }

    public boolean isOptional() {
        return this == RECOMMENDED || this == ENHANCES;
    }
}