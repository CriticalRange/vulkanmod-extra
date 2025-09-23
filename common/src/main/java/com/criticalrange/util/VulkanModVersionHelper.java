package com.criticalrange.util;

import com.criticalrange.VulkanModExtra;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

/**
 * Utility class for detecting VulkanMod version and feature availability
 */
public class VulkanModVersionHelper {
    private static String vulkanModVersion = null;

    /**
     * Get the currently loaded VulkanMod version
     */
    public static String getVulkanModVersion() {
        if (vulkanModVersion == null) {
            Optional<ModContainer> vulkanMod = FabricLoader.getInstance().getModContainer("vulkanmod");
            if (vulkanMod.isPresent()) {
                vulkanModVersion = vulkanMod.get().getMetadata().getVersion().getFriendlyString();
                VulkanModExtra.LOGGER.info("Detected VulkanMod version: {}", vulkanModVersion);
            } else {
                vulkanModVersion = "unknown";
                VulkanModExtra.LOGGER.warn("VulkanMod not found, version detection failed");
            }
        }
        return vulkanModVersion;
    }

    /**
     * Check if VulkanMod version is at least the specified version
     */
    public static boolean isVulkanModVersionAtLeast(String targetVersion) {
        String currentVersion = getVulkanModVersion();
        if ("unknown".equals(currentVersion)) {
            return false;
        }

        try {
            return compareVersions(currentVersion, targetVersion) >= 0;
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to compare VulkanMod versions: {} vs {}", currentVersion, targetVersion, e);
            return false;
        }
    }



    /**
     * Compare two version strings (e.g., "0.5.5" vs "0.5.3")
     * Returns: negative if v1 < v2, zero if v1 == v2, positive if v1 > v2
     */
    private static int compareVersions(String v1, String v2) {
        // Remove any pre-release suffixes (e.g., "0.5.5-beta" -> "0.5.5")
        v1 = v1.split("-")[0];
        v2 = v2.split("-")[0];

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (part1 != part2) {
                return Integer.compare(part1, part2);
            }
        }

        return 0;
    }

    /**
     * Get a summary of VulkanMod compatibility
     */
    public static String getCompatibilitySummary() {
        return String.format("VulkanMod %s", getVulkanModVersion());
    }

    /**
     * Reset cached values (for testing)
     */
    public static void reset() {
        vulkanModVersion = null;
    }
}