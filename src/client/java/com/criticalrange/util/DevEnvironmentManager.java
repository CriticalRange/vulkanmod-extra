package com.criticalrange.util;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.File;

/**
 * Development Environment Manager for VulkanMod Extra
 * Provides in-game version information and debugging tools
 * 
 * This class helps developers during development by providing:
 * - Current version information in-game
 * - Warning about version mismatches
 * - List of active overrides in debug mode
 * - Command for configuration reloading
 */
public class DevEnvironmentManager {
    
    private static boolean debugMode = false;
    private static boolean initialized = false;
    
    /**
     * Initialize the dev environment manager
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        logVersionInfo();
        initialized = true;
    }
    
    /**
     * Log version information to console
     */
    public static void logVersionInfo() {
        String versionInfo = VersionHelper.getDebugInfo();
        VulkanModExtra.LOGGER.info("[VulkanMod Extra] " + versionInfo);
        
        // Log current overrides
        logActiveOverrides();
        
        // Log cache statistics if available
        // System.out.println("[VulkanMod Extra] " + MappingHelper.getCacheStats());
    }
    
    /**
     * Log information about active overrides
     */
    public static void logActiveOverrides() {
        try {
            String versionKey = VersionHelper.getCurrentVersion().replace('.', '_');
            File overrideDir = new File("src/overrides/v" + versionKey);
            
            if (overrideDir.exists()) {
                File javaDir = new File(overrideDir, "java");
                File resourcesDir = new File(overrideDir, "resources");
                
                int javaFiles = countFiles(javaDir);
                int resourceFiles = countFiles(resourcesDir);
                
                VulkanModExtra.LOGGER.info("[VulkanMod Extra] Active overrides for v{}: {} Java files, {} resource files", 
                    versionKey, javaFiles, resourceFiles);
            } else {
                VulkanModExtra.LOGGER.info("[VulkanMod Extra] No active overrides for v{}", versionKey);
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("[VulkanMod Extra] Could not log override information: {}", e.getMessage());
        }
    }
    
    /**
     * Count files in a directory recursively
     */
    private static int countFiles(File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return 0;
        }
        
        int count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                count += countFiles(file);
            } else if (file.getName().endsWith(".java")) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Check for version mismatches and warn if needed
     */
    public static void checkVersionMismatch() {
        // This could check for development vs production versions
        // or warn about using unsupported Minecraft versions
        if (VersionHelper.IS_1_21_1) {
            VulkanModExtra.LOGGER.info("[VulkanMod Extra] Running on Minecraft 1.21.1");
        } else if (VersionHelper.IS_1_21_2) {
            VulkanModExtra.LOGGER.info("[VulkanMod Extra] Running on Minecraft 1.21.2");
        } else if (VersionHelper.IS_1_21_3) {
            VulkanModExtra.LOGGER.info("[VulkanMod Extra] Running on Minecraft 1.21.3");
        } else if (VersionHelper.IS_1_21_4) {
            VulkanModExtra.LOGGER.info("[VulkanMod Extra] Running on Minecraft 1.21.4");
        } else {
            VulkanModExtra.LOGGER.warn("[VulkanMod Extra] Running on unsupported Minecraft version: {}", VersionHelper.getCurrentVersion());
        }
    }
    
    /**
     * Show version information in-game when F3 + V is pressed
     */
    public static void showVersionInfoInGame() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) {
                return;
            }
            
            String versionInfo = String.format("VulkanMod Extra v%s (MC %s)", 
                com.criticalrange.VulkanModExtra.class.getPackage().getImplementationVersion() != null ? 
                    com.criticalrange.VulkanModExtra.class.getPackage().getImplementationVersion() : "dev",
                VersionHelper.getCurrentVersion());
            
            if (hasActiveOverrides()) {
                versionInfo += " [OVERRIDES ACTIVE]";
            }
            
            if (debugMode) {
                versionInfo += String.format(" | Debug Mode | Cache: %s", MappingHelper.getCacheStats());
            }
            
            client.player.sendMessage(Text.literal(versionInfo), false);
            
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("[VulkanMod Extra] Could not show version info in-game: {}", e.getMessage());
        }
    }
    
    /**
     * Check if there are active overrides for the current version
     */
    public static boolean hasActiveOverrides() {
        try {
            String versionKey = VersionHelper.getCurrentVersion().replace('.', '_');
            File overrideDir = new File("src/overrides/v" + versionKey);
            return overrideDir.exists();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Toggle debug mode
     */
    public static void toggleDebugMode() {
        debugMode = !debugMode;
        VulkanModExtra.LOGGER.info("[VulkanMod Extra] Debug mode: {}", debugMode ? "ON" : "OFF");
        
        if (debugMode) {
            logDetailedDebugInfo();
        }
    }
    
    /**
     * Log detailed debug information
     */
    public static void logDetailedDebugInfo() {
        VulkanModExtra.LOGGER.info("[VulkanMod Extra] === DETAILED DEBUG INFO ===");
        VulkanModExtra.LOGGER.info("[VulkanMod Extra] {}", VersionHelper.getDebugInfo());
        VulkanModExtra.LOGGER.info("[VulkanMod Extra] {}", MappingHelper.getCacheStats());
        logActiveOverrides();
        logSystemInfo();
    }
    
    /**
     * Log system information
     */
    private static void logSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        long memoryMB = runtime.totalMemory() / (1024 * 1024);
        long maxMemoryMB = runtime.maxMemory() / (1024 * 1024);
        long freeMemoryMB = runtime.freeMemory() / (1024 * 1024);
        
        VulkanModExtra.LOGGER.info("[VulkanMod Extra] Memory Usage: {} MB used, {} MB free, {} MB max", 
            memoryMB, freeMemoryMB, maxMemoryMB);
    }
    
    /**
     * Handle the /vulkanmod-extra reload command
     */
    public static boolean handleReloadCommand() {
        try {
            VulkanModExtra.LOGGER.info("[VulkanMod Extra] Reloading configuration...");
            
            // Clear mapping caches
            MappingHelper.clearCache();
            
            // Reload configuration
            if (com.criticalrange.VulkanModExtra.configManager != null) {
                com.criticalrange.VulkanModExtra.CONFIG = com.criticalrange.VulkanModExtra.configManager.loadConfig();
                VulkanModExtra.LOGGER.info("[VulkanMod Extra] Configuration reloaded successfully");
            }
            
            logVersionInfo();
            return true;
            
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("[VulkanMod Extra] Failed to reload configuration: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get debug mode status
     */
    public static boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * Set debug mode
     */
    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
        if (enabled) {
            logDetailedDebugInfo();
        }
    }
    
    /**
     * Show help information for development commands
     */
    public static void showHelp() {
        String help = """
            [VulkanMod Extra] Development Commands:
            /vulkanmod-extra reload - Reload configuration
            /vulkanmod-extra debug - Toggle debug mode
            /vulkanmod-extra version - Show version information
            /vulkanmod-extra overrides - List active overrides
            """;
        
        VulkanModExtra.LOGGER.info(help);
    }
}