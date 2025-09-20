package com.criticalrange.util;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Runtime version detection helper for VulkanMod Extra
 * Provides easy access to Minecraft version information and boolean flags
 *
 * This class uses FabricLoader to detect the current Minecraft version at runtime
 * and provides convenient boolean flags for version-specific code.
 *
 * Supported versions: 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5
 */
public final class VersionHelper {
    // Supported versions
    private static final String MINECRAFT_1_21_1 = "1.21.1";
    private static final String MINECRAFT_1_21_2 = "1.21.2";
    private static final String MINECRAFT_1_21_3 = "1.21.3";
    private static final String MINECRAFT_1_21_4 = "1.21.4";
    private static final String MINECRAFT_1_21_5 = "1.21.5";

    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList(
        MINECRAFT_1_21_1, MINECRAFT_1_21_2, MINECRAFT_1_21_3, MINECRAFT_1_21_4, MINECRAFT_1_21_5
    );
    
    private static final String CURRENT_VERSION;
    private static final int[] VERSION_PARTS;
    
    // Version flags for easy boolean checks
    public static final boolean IS_1_21_1;
    public static final boolean IS_1_21_2;
    public static final boolean IS_1_21_3;
    public static final boolean IS_1_21_4;
    public static final boolean IS_1_21_5;
    public static final boolean IS_1_21_X;

    // Version range checks
    public static final boolean IS_PRE_1_21_2;
    public static final boolean IS_PRE_1_21_3;
    public static final boolean IS_PRE_1_21_4;
    public static final boolean IS_PRE_1_21_5;
    public static final boolean IS_POST_1_21_1;
    public static final boolean IS_POST_1_21_2;
    public static final boolean IS_POST_1_21_3;
    public static final boolean IS_POST_1_21_4;
    
    // Initialize version information
    static {
        CURRENT_VERSION = detectMinecraftVersion();
        VERSION_PARTS = parseVersionParts(CURRENT_VERSION);
        
        // Set version flags
        IS_1_21_1 = CURRENT_VERSION.equals("1.21.1");
        IS_1_21_2 = CURRENT_VERSION.equals("1.21.2");
        IS_1_21_3 = CURRENT_VERSION.equals("1.21.3");
        IS_1_21_4 = CURRENT_VERSION.equals("1.21.4");
        IS_1_21_5 = CURRENT_VERSION.equals("1.21.5");
        IS_1_21_X = VERSION_PARTS[0] == 1 && VERSION_PARTS[1] == 21;

        // Set range flags
        IS_PRE_1_21_2 = compareVersions(CURRENT_VERSION, "1.21.2") < 0;
        IS_PRE_1_21_3 = compareVersions(CURRENT_VERSION, "1.21.3") < 0;
        IS_PRE_1_21_4 = compareVersions(CURRENT_VERSION, "1.21.4") < 0;
        IS_PRE_1_21_5 = compareVersions(CURRENT_VERSION, "1.21.5") < 0;
        IS_POST_1_21_1 = compareVersions(CURRENT_VERSION, "1.21.1") > 0;
        IS_POST_1_21_2 = compareVersions(CURRENT_VERSION, "1.21.2") > 0;
        IS_POST_1_21_3 = compareVersions(CURRENT_VERSION, "1.21.3") > 0;
        IS_POST_1_21_4 = compareVersions(CURRENT_VERSION, "1.21.4") > 0;
    }
    
    /**
     * Detect the current Minecraft version from FabricLoader
     * @return Minecraft version string (e.g., "1.21.1")
     */
    public static String detectMinecraftVersion() {
        try {
            // Try to get version from FabricLoader metadata
            String version = FabricLoader.getInstance().getModContainer("minecraft")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
            
            // Extract version number from full version string
            // Minecraft versions might be like "1.21.1", "1.21.1-pre1", etc.
            Pattern versionPattern = Pattern.compile("(\\d+\\.\\d+(?:\\.\\d+)?)(?:-|$)");
            Matcher matcher = versionPattern.matcher(version);
            
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            return version;
        } catch (Exception e) {
            // Fallback to a reasonable default if detection fails
            return "1.21.1";
        }
    }
    
    /**
     * Parse version string into integer array
     * @param version Version string (e.g., "1.21.1")
     * @return Array of version parts [major, minor, patch]
     */
    private static int[] parseVersionParts(String version) {
        String[] parts = version.split("\\.");
        int[] result = new int[3];
        
        // Major version
        result[0] = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        // Minor version  
        result[1] = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        // Patch version
        result[2] = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        
        return result;
    }
    
    /**
     * Compare two version strings
     * @param version1 First version string
     * @param version2 Second version string
     * @return -1 if version1 < version2, 0 if equal, 1 if version1 > version2
     */
    public static int compareVersions(String version1, String version2) {
        int[] parts1 = parseVersionParts(version1);
        int[] parts2 = parseVersionParts(version2);
        
        // Compare major version
        if (parts1[0] != parts2[0]) {
            return Integer.compare(parts1[0], parts2[0]);
        }
        
        // Compare minor version
        if (parts1[1] != parts2[1]) {
            return Integer.compare(parts1[1], parts2[1]);
        }
        
        // Compare patch version
        return Integer.compare(parts1[2], parts2[2]);
    }
    
    /**
     * Get the current Minecraft version string
     * @return Current version (e.g., "1.21.1")
     */
    public static String getCurrentVersion() {
        return CURRENT_VERSION;
    }
    
    /**
     * Get version parts as integers
     * @return Array [major, minor, patch]
     */
    public static int[] getVersionParts() {
        return VERSION_PARTS.clone(); // Return copy to prevent modification
    }
    
    /**
     * Check if current version is in a range
     * @param minVersion Minimum version (inclusive)
     * @param maxVersion Maximum version (inclusive)
     * @return true if current version is in range
     */
    public static boolean isVersionInRange(String minVersion, String maxVersion) {
        return compareVersions(CURRENT_VERSION, minVersion) >= 0 &&
               compareVersions(CURRENT_VERSION, maxVersion) <= 0;
    }
    
    /**
     * Check if current version matches any of the provided versions
     * @param versions Array of version strings to check against
     * @return true if current version matches any provided version
     */
    public static boolean isVersion(String... versions) {
        for (String version : versions) {
            if (CURRENT_VERSION.equals(version)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get development information for debugging
     * @return String with version information
     */
    public static String getDebugInfo() {
        return String.format("Minecraft Version: %s (Parsed as: %d.%d.%d)", 
            CURRENT_VERSION, VERSION_PARTS[0], VERSION_PARTS[1], VERSION_PARTS[2]);
    }
    
    /**
     * Log version information to console for debugging
     */
    public static void logVersionInfo() {
        if (com.criticalrange.VulkanModExtra.LOGGER != null) {
            com.criticalrange.VulkanModExtra.LOGGER.info("{}", getDebugInfo());
            com.criticalrange.VulkanModExtra.LOGGER.debug("Version Flags: 1.21.1={}, 1.21.2={}, 1.21.3={}, 1.21.4={}, 1.21.5={}, 1.21.X={}",
                IS_1_21_1, IS_1_21_2, IS_1_21_3, IS_1_21_4, IS_1_21_5, IS_1_21_X);
        }
    }
    
    // Override system support methods
    /**
     * Get the version key for override directories (e.g., "1.21.1" -> "1_21_1")
     */
    public static String getOverrideVersionKey() {
        return CURRENT_VERSION.replace(".", "_");
    }
    
    /**
     * Check if override directory exists for the current version
     */
    public static boolean hasOverrideDirectory() {
        String key = getOverrideVersionKey();
        String overridePath = "src/overrides/v" + key;
        return new java.io.File(overridePath).exists();
    }
    
    /**
     * Check if a version is supported
     */
    public static boolean isSupported(String version) {
        return SUPPORTED_VERSIONS.contains(version);
    }
    
    /**
     * Get the list of supported versions
     */
    public static List<String> getSupportedVersions() {
        return SUPPORTED_VERSIONS;
    }
    
    /**
     * Check if the current version is supported
     */
    public static boolean isCurrentVersionSupported() {
        return isSupported(CURRENT_VERSION);
    }
    
    /**
     * Get a user-friendly version name
     */
    public static String getFriendlyName() {
        switch (CURRENT_VERSION) {
            case MINECRAFT_1_21_1:
                return "Minecraft 1.21.1";
            case MINECRAFT_1_21_2:
                return "Minecraft 1.21.2";
            case MINECRAFT_1_21_3:
                return "Minecraft 1.21.3";
            case MINECRAFT_1_21_4:
                return "Minecraft 1.21.4";
            case MINECRAFT_1_21_5:
                return "Minecraft 1.21.5";
            default:
                return "Minecraft " + CURRENT_VERSION;
        }
    }
    
    /**
     * Get version information for debugging
     */
    public static String getVersionInfo() {
        return String.format("Version: %s (Friendly: %s, Supported: %b, OverrideKey: %s)",
            CURRENT_VERSION, getFriendlyName(), isCurrentVersionSupported(), getOverrideVersionKey());
    }
    
    /**
     * Check if current version is at least the specified version
     */
    public static boolean isAtLeast(String version) {
        return compareVersions(CURRENT_VERSION, version) >= 0;
    }
    
    /**
     * Check if current version is less than the specified version
     */
    public static boolean isLessThan(String version) {
        return compareVersions(CURRENT_VERSION, version) < 0;
    }
}