package com.criticalrange.core.dependency;

import java.util.Objects;

/**
 * Represents a dependency between features
 */
public class FeatureDependency {
    private final String dependentFeature;
    private final String requiredFeature;
    private final DependencyType type;
    private final String description;
    private final boolean optional;

    public FeatureDependency(String dependentFeature, String requiredFeature,
                            DependencyType type, String description) {
        this(dependentFeature, requiredFeature, type, description, false);
    }

    public FeatureDependency(String dependentFeature, String requiredFeature,
                            DependencyType type, String description, boolean optional) {
        this.dependentFeature = dependentFeature;
        this.requiredFeature = requiredFeature;
        this.type = type;
        this.description = description;
        this.optional = optional;
    }

    public String getDependentFeature() {
        return dependentFeature;
    }

    public String getRequiredFeature() {
        return requiredFeature;
    }

    public DependencyType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%s%s)", dependentFeature, requiredFeature,
                           type, optional ? " optional" : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FeatureDependency that = (FeatureDependency) obj;
        return dependentFeature.equals(that.dependentFeature) &&
               requiredFeature.equals(that.requiredFeature) &&
               type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependentFeature, requiredFeature, type);
    }
}