package com.criticalrange.core.dependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the dependency graph between features
 * Handles dependency resolution, cycle detection, and loading order
 */
public class DependencyGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra Dependency Graph");
    private static final DependencyGraph INSTANCE = new DependencyGraph();

    private final Map<String, FeatureDependency> dependencies = new HashMap<>();
    private final Map<String, Set<String>> adjacencyList = new HashMap<>();
    private final Map<String, Set<String>> reverseAdjacencyList = new HashMap<>();

    private DependencyGraph() {
        // Private constructor for singleton
    }

    public static DependencyGraph getInstance() {
        return INSTANCE;
    }

    /**
     * Create a new instance for validation purposes
     */
    public static DependencyGraph createValidationInstance() {
        return new DependencyGraph();
    }

    /**
     * Add a dependency to the graph
     */
    public void addDependency(FeatureDependency dependency) {
        String key = dependency.getDependentFeature() + "->" + dependency.getRequiredFeature();
        dependencies.put(key, dependency);

        // Update adjacency lists
        adjacencyList.computeIfAbsent(dependency.getDependentFeature(), k -> new HashSet<>())
                    .add(dependency.getRequiredFeature());

        reverseAdjacencyList.computeIfAbsent(dependency.getRequiredFeature(), k -> new HashSet<>())
                           .add(dependency.getDependentFeature());

        LOGGER.debug("Added dependency: {}", dependency);
    }

    /**
     * Remove a dependency from the graph
     */
    public void removeDependency(String dependentFeature, String requiredFeature) {
        String key = dependentFeature + "->" + requiredFeature;
        FeatureDependency dependency = dependencies.remove(key);

        if (dependency != null) {
            adjacencyList.getOrDefault(dependentFeature, Collections.emptySet())
                         .remove(requiredFeature);

            reverseAdjacencyList.getOrDefault(requiredFeature, Collections.emptySet())
                               .remove(dependentFeature);

            // Clean up empty sets
            if (adjacencyList.containsKey(dependentFeature) && adjacencyList.get(dependentFeature).isEmpty()) {
                adjacencyList.remove(dependentFeature);
            }

            if (reverseAdjacencyList.containsKey(requiredFeature) && reverseAdjacencyList.get(requiredFeature).isEmpty()) {
                reverseAdjacencyList.remove(requiredFeature);
            }

            LOGGER.debug("Removed dependency: {} -> {}", dependentFeature, requiredFeature);
        }
    }

    /**
     * Get all dependencies for a feature
     */
    public List<FeatureDependency> getDependenciesForFeature(String featureId) {
        return dependencies.values().stream()
                          .filter(dep -> dep.getDependentFeature().equals(featureId))
                          .collect(Collectors.toList());
    }

    /**
     * Get all features that depend on the given feature
     */
    public List<FeatureDependency> getDependentsOfFeature(String featureId) {
        return dependencies.values().stream()
                          .filter(dep -> dep.getRequiredFeature().equals(featureId))
                          .collect(Collectors.toList());
    }

    /**
     * Check for dependency cycles
     */
    public List<String> detectCycles() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        List<String> cycles = new ArrayList<>();

        for (String feature : adjacencyList.keySet()) {
            if (!visited.contains(feature)) {
                detectCyclesDFS(feature, visited, recursionStack, cycles, new ArrayList<>());
            }
        }

        return cycles;
    }

    private void detectCyclesDFS(String feature, Set<String> visited, Set<String> recursionStack,
                               List<String> cycles, List<String> path) {
        visited.add(feature);
        recursionStack.add(feature);
        path.add(feature);

        for (String dependency : adjacencyList.getOrDefault(feature, Collections.emptySet())) {
            if (!visited.contains(dependency)) {
                detectCyclesDFS(dependency, visited, recursionStack, cycles, path);
            } else if (recursionStack.contains(dependency)) {
                // Found a cycle
                int cycleStart = path.indexOf(dependency);
                String cycle = String.join(" -> ", path.subList(cycleStart, path.size())) + " -> " + dependency;
                cycles.add(cycle);
            }
        }

        recursionStack.remove(feature);
        path.remove(path.size() - 1);
    }

    /**
     * Resolve dependencies and return the optimal loading order
     */
    public List<String> resolveLoadingOrder(Set<String> availableFeatures) {
        List<String> result = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        Map<String, Integer> inDegree = new HashMap<>();

        // Calculate in-degree for each feature
        for (String feature : availableFeatures) {
            inDegree.put(feature, 0);
        }

        for (String feature : availableFeatures) {
            for (String dependency : adjacencyList.getOrDefault(feature, Collections.emptySet())) {
                if (availableFeatures.contains(dependency)) {
                    inDegree.put(dependency, inDegree.getOrDefault(dependency, 0) + 1);
                }
            }
        }

        // Topological sort
        Queue<String> queue = new LinkedList<>();
        for (String feature : availableFeatures) {
            if (inDegree.getOrDefault(feature, 0) == 0) {
                queue.offer(feature);
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);
            processed.add(current);

            for (String neighbor : adjacencyList.getOrDefault(current, Collections.emptySet())) {
                if (availableFeatures.contains(neighbor)) {
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                    if (inDegree.get(neighbor) == 0) {
                        queue.offer(neighbor);
                    }
                }
            }
        }

        // Check if we processed all features
        if (processed.size() != availableFeatures.size()) {
            LOGGER.warn("Could not resolve dependencies for all features. Cycles may exist.");
            // Add remaining features in arbitrary order
            for (String feature : availableFeatures) {
                if (!processed.contains(feature)) {
                    result.add(feature);
                }
            }
        }

        return result;
    }

    /**
     * Validate dependencies against available features
     */
    public DependencyValidationResult validateDependencies(Set<String> availableFeatures) {
        List<String> missingRequired = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (FeatureDependency dependency : dependencies.values()) {
            if (!availableFeatures.contains(dependency.getRequiredFeature())) {
                if (dependency.getType() == DependencyType.REQUIRED) {
                    missingRequired.add(dependency.getRequiredFeature());
                } else if (dependency.getType() == DependencyType.RECOMMENDED) {
                    warnings.add("Recommended dependency '" + dependency.getRequiredFeature() +
                               "' for '" + dependency.getDependentFeature() + "' is not available");
                }
            }

            if (dependency.getType() == DependencyType.CONFLICT &&
                availableFeatures.contains(dependency.getDependentFeature()) &&
                availableFeatures.contains(dependency.getRequiredFeature())) {
                conflicts.add(String.format("Conflict between '%s' and '%s'",
                                          dependency.getDependentFeature(),
                                          dependency.getRequiredFeature()));
            }
        }

        // Check for cycles
        List<String> cycles = detectCycles();
        if (!cycles.isEmpty()) {
            warnings.add("Dependency cycles detected: " + String.join("; ", cycles));
        }

        return new DependencyValidationResult(missingRequired, conflicts, warnings);
    }

    /**
     * Get all dependencies in the graph
     */
    public Collection<FeatureDependency> getAllDependencies() {
        return new ArrayList<>(dependencies.values());
    }

    /**
     * Clear all dependencies
     */
    public void clear() {
        dependencies.clear();
        adjacencyList.clear();
        reverseAdjacencyList.clear();
    }

    /**
     * Validation result record
     */
    public record DependencyValidationResult(List<String> missingRequired,
                                            List<String> conflicts,
                                            List<String> warnings) {
        public boolean isValid() {
            return missingRequired.isEmpty() && conflicts.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            if (isValid()) {
                sb.append("Dependency validation passed");
            } else {
                sb.append("Dependency validation failed");
                if (!missingRequired.isEmpty()) {
                    sb.append("\nMissing required features: ").append(String.join(", ", missingRequired));
                }
                if (!conflicts.isEmpty()) {
                    sb.append("\nConflicts: ").append(String.join(", ", conflicts));
                }
            }
            if (hasWarnings()) {
                sb.append("\nWarnings: ").append(String.join(", ", warnings));
            }
            return sb.toString();
        }
    }
}