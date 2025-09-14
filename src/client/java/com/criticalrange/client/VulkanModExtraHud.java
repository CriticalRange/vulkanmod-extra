package com.criticalrange.client;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * HUD overlay system for VulkanMod Extra features like FPS display and coordinates
 * Based on sodium-extra's HUD system
 */
public class VulkanModExtraHud {
    private final MinecraftClient minecraft;
    private int fps = 0;
    private long lastUpdateTime = 0;
    
    // FPS tracking for extended modes
    private int minFps = Integer.MAX_VALUE;
    private int maxFps = 0;
    private final java.util.List<Integer> fpsHistory = new java.util.ArrayList<>();
    private static final int HISTORY_SIZE = 1000; // Track last 1000 frames for percentile calculation
    
    // Memory tracking
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private long lastVramCheck = 0;
    private String cachedVramInfo = "";
    private static final long VRAM_UPDATE_INTERVAL = 1000; // Update VRAM info every 1 second

    public VulkanModExtraHud() {
        this.minecraft = MinecraftClient.getInstance();
    }

    public void onHudRender(DrawContext drawContext, float partialTicks) {
        if (minecraft.player == null) return;
        
        // Show our HUD when F3 debug screen is NOT open, or show alongside it
        // For now, let's always show it to test if it works

        // Update FPS tracking every frame for accurate percentiles
        int currentFps = minecraft.getCurrentFps();
        fpsHistory.add(currentFps);
        if (fpsHistory.size() > HISTORY_SIZE) {
            fpsHistory.remove(0);
        }
        
        // Update FPS counter and stats every second
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= 1000) { // Update every second
            fps = currentFps;
            
            // Update min/max
            if (currentFps < minFps) minFps = currentFps;
            if (currentFps > maxFps) maxFps = currentFps;
            
            lastUpdateTime = currentTime;
            
            // Debug log to see if HUD is being called and what the config values are (only once per second)
            var config = VulkanModExtra.CONFIG.extraSettings;
            var fpsMode = config.fpsDisplayMode;
            VulkanModExtra.LOGGER.debug("FPS updated - showFps: {}, fpsDisplayMode: {}, FPS: {}", config.showFps, fpsMode, fps);
        }

        renderOverlay(drawContext);
    }

    private void renderOverlay(DrawContext drawContext) {
        var config = VulkanModExtra.CONFIG.extraSettings;

        // if none is true: nothing to render
        if (!config.showFps && !config.showCoords) {
            return;
        }

        int screenWidth = minecraft.getWindow().getScaledWidth();
        int screenHeight = minecraft.getWindow().getScaledHeight();

        // Calculate overlay position based on corner setting
        var overlayCorner = config.overlayCorner;
        int x = getOverlayX(screenWidth, overlayCorner);
        int y = getOverlayY(screenHeight, overlayCorner);

        int lineHeight = 10;
        int currentY = y;

        // FPS display
        if (config.showFps) {
            currentY += renderFpsDisplay(drawContext, x, currentY, lineHeight, config);
        }

        // Coordinates display
        if (config.showCoords) {
            PlayerEntity player = minecraft.player;
            String coordsText = String.format("XYZ: %.1f / %.1f / %.1f",
                player.getX(), player.getY(), player.getZ());
            var textContrast = config.textContrast;
            drawText(drawContext, coordsText, x, currentY, textContrast);
        }
    }

    private int getOverlayX(int screenWidth, com.criticalrange.config.VulkanModExtraConfig.OverlayCorner corner) {
        return switch (corner) {
            case TOP_LEFT, BOTTOM_LEFT -> 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - 100;
        };
    }

    private int getOverlayY(int screenHeight, com.criticalrange.config.VulkanModExtraConfig.OverlayCorner corner) {
        // Calculate required height based on what's being displayed
        int requiredHeight = calculateRequiredHeight();
        
        return switch (corner) {
            case TOP_LEFT, TOP_RIGHT -> 2;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - requiredHeight - 2;
        };
    }
    
    private int calculateRequiredHeight() {
        var config = VulkanModExtra.CONFIG.extraSettings;
        int lines = 0;
        
        if (config.showFps) {
            lines += switch (config.fpsDisplayMode) {
                case BASIC -> 1;
                case EXTENDED -> 2; // FPS + Memory
                case DETAILED -> 3; // FPS + Percentiles + Memory
            };
        }
        
        if (config.showCoords) {
            lines += 1;
        }
        
        return lines * 10; // 10 pixels per line
    }

    private void drawText(DrawContext drawContext, String text, int x, int y,
                         com.criticalrange.config.VulkanModExtraConfig.TextContrast contrast) {
        int color = 0xFFFFFF; // White text

        switch (contrast) {
            case BACKGROUND -> {
                // Draw background
                drawContext.fill(x - 1, y - 1, x + minecraft.textRenderer.getWidth(text) + 1, y + 9, 0x80000000);
            }
            case SHADOW -> {
                // Draw with shadow
                drawContext.drawText(minecraft.textRenderer, text, x + 1, y + 1, 0x000000, false);
            }
        }

        drawContext.drawText(minecraft.textRenderer, text, x, y, color, false);
    }
    
    private int renderFpsDisplay(DrawContext drawContext, int x, int y, int lineHeight,
                                com.criticalrange.config.VulkanModExtraConfig.ExtraSettings config) {
        int linesRendered = 0;
        var fpsMode = config.fpsDisplayMode;
        var textContrast = config.textContrast;
        
        switch (fpsMode) {
            case BASIC -> {
                String fpsText = "FPS: " + fps;
                drawText(drawContext, fpsText, x, y, textContrast);
                linesRendered = 1;
            }
            case EXTENDED -> {
                // Calculate average from history
                int avgFps = fpsHistory.isEmpty() ? fps : 
                    (int) fpsHistory.stream().mapToInt(Integer::intValue).average().orElse(fps);
                
                // Line 1: FPS stats
                String fpsText = String.format("FPS: %d (min: %d, avg: %d, max: %d)", 
                    fps, minFps, avgFps, maxFps);
                drawText(drawContext, fpsText, x, y, textContrast);
                
                // Line 2: Memory info
                String memoryText = getMemoryInfo();
                drawText(drawContext, memoryText, x, y + lineHeight, textContrast);
                linesRendered = 2;
            }
            case DETAILED -> {
                // Calculate average from history
                int avgFps = fpsHistory.isEmpty() ? fps : 
                    (int) fpsHistory.stream().mapToInt(Integer::intValue).average().orElse(fps);
                
                // Line 1: Basic stats
                String fpsText = String.format("FPS: %d (min: %d, avg: %d, max: %d)", 
                    fps, minFps, avgFps, maxFps);
                drawText(drawContext, fpsText, x, y, textContrast);
                
                // Line 2: Percentiles
                int low1Percent = getPercentile(1.0);
                int low01Percent = getPercentile(0.1);
                String percentileText = String.format("Low 1%%: %d, Low 0.1%%: %d", 
                    low1Percent, low01Percent);
                drawText(drawContext, percentileText, x, y + lineHeight, textContrast);
                
                // Line 3: Memory info
                String memoryText = getMemoryInfo();
                drawText(drawContext, memoryText, x, y + (lineHeight * 2), textContrast);
                linesRendered = 3;
            }
        }
        
        return linesRendered * lineHeight;
    }
    
    private int getPercentile(double percentile) {
        if (fpsHistory.isEmpty()) return fps;
        
        java.util.List<Integer> sortedHistory = new java.util.ArrayList<>(fpsHistory);
        java.util.Collections.sort(sortedHistory);
        
        int index = (int) Math.ceil(sortedHistory.size() * percentile / 100.0) - 1;
        index = Math.max(0, Math.min(index, sortedHistory.size() - 1));
        
        return sortedHistory.get(index);
    }
    
    /**
     * Gets GPU VRAM and memory usage information using VulkanMod APIs
     */
    private String getMemoryInfo() {
        long currentTime = System.currentTimeMillis();
        
        // Update VRAM info only periodically to reduce performance impact
        if (currentTime - lastVramCheck >= VRAM_UPDATE_INTERVAL) {
            try {
                // Try to get VulkanMod GPU memory information first
                String vramInfo = getVulkanModVRAMInfo();
                
                if (vramInfo != null) {
                    cachedVramInfo = vramInfo;
                } else {
                    // Fallback to Java heap memory if VulkanMod APIs aren't available
                    cachedVramInfo = getSystemMemoryFallback();
                }
                
            } catch (Exception e) {
                VulkanModExtra.LOGGER.debug("Error getting memory info: " + e.getMessage());
                cachedVramInfo = "Memory: N/A";
            }
            
            lastVramCheck = currentTime;
        }
        
        return cachedVramInfo;
    }
    
    /**
     * Gets actual GPU VRAM usage using VulkanMod's memory manager APIs
     */
    private String getVulkanModVRAMInfo() {
        try {
            // Access VulkanMod's memory manager via reflection for safety
            Class<?> memoryManagerClass = Class.forName("net.vulkanmod.vulkan.memory.MemoryManager");
            Class<?> vulkanClass = Class.forName("net.vulkanmod.vulkan.Vulkan");
            
            // Get MemoryManager instance
            java.lang.reflect.Method getInstanceMethod = memoryManagerClass.getDeclaredMethod("getInstance");
            Object memoryManager = getInstanceMethod.invoke(null);
            
            // Get VRAM information
            java.lang.reflect.Method getAllocatedMethod = memoryManagerClass.getDeclaredMethod("getAllocatedDeviceMemoryMB");
            java.lang.reflect.Method getTotalMethod = memoryManagerClass.getDeclaredMethod("getDeviceMemoryMB");
            java.lang.reflect.Method getNativeMethod = memoryManagerClass.getDeclaredMethod("getNativeMemoryMB");
            
            int allocatedVRAM = (Integer) getAllocatedMethod.invoke(memoryManager);
            int totalVRAM = (Integer) getTotalMethod.invoke(memoryManager);
            int nativeMemory = (Integer) getNativeMethod.invoke(memoryManager);
            
            // Try to get device name for context
            String deviceName = getGPUDeviceName();
            
            // Format the memory information
            if (deviceName != null && !deviceName.isEmpty()) {
                return String.format("VRAM: %dMB/%dMB (%s) | Native: %dMB", 
                    allocatedVRAM, totalVRAM, deviceName, nativeMemory);
            } else {
                return String.format("VRAM: %dMB/%dMB | Native: %dMB", 
                    allocatedVRAM, totalVRAM, nativeMemory);
            }
            
        } catch (ClassNotFoundException e) {
            // VulkanMod not available, this is expected in some environments
            VulkanModExtra.LOGGER.debug("VulkanMod classes not found, falling back to system memory");
            return null;
        } catch (Exception e) {
            VulkanModExtra.LOGGER.debug("Failed to get VulkanMod VRAM info: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets GPU device name for display context
     */
    private String getGPUDeviceName() {
        try {
            Class<?> vulkanClass = Class.forName("net.vulkanmod.vulkan.Vulkan");
            java.lang.reflect.Method getDeviceMethod = vulkanClass.getDeclaredMethod("getDevice");
            Object device = getDeviceMethod.invoke(null);
            
            java.lang.reflect.Field deviceNameField = device.getClass().getDeclaredField("deviceName");
            String fullName = (String) deviceNameField.get(device);
            
            // Truncate device name for display (e.g., "NVIDIA GeForce RTX 4090" -> "RTX 4090")
            if (fullName != null) {
                if (fullName.contains("RTX")) {
                    return fullName.substring(fullName.indexOf("RTX"));
                } else if (fullName.contains("GTX")) {
                    return fullName.substring(fullName.indexOf("GTX"));
                } else if (fullName.contains("RX ")) {
                    return fullName.substring(fullName.indexOf("RX "));
                } else if (fullName.contains("Arc ")) {
                    return fullName.substring(fullName.indexOf("Arc "));
                } else {
                    // Take last part of device name if no known GPU series found
                    String[] parts = fullName.split(" ");
                    return parts.length > 1 ? parts[parts.length - 1] : fullName;
                }
            }
        } catch (Exception ignored) {}
        
        return null;
    }
    
    /**
     * Fallback system memory info when VulkanMod APIs aren't available
     */
    private String getSystemMemoryFallback() {
        try {
            MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
            long usedHeap = heapMemory.getUsed() / (1024 * 1024);
            long maxHeap = heapMemory.getMax() / (1024 * 1024);
            
            // Try to get system memory
            long totalSystemMemory = getTotalSystemMemory();
            long usedSystemMemory = getUsedSystemMemory();
            
            if (totalSystemMemory > 0 && usedSystemMemory > 0) {
                return String.format("RAM: %dMB/%dMB | Heap: %dMB/%dMB", 
                    usedSystemMemory, totalSystemMemory, usedHeap, maxHeap);
            } else {
                return String.format("Heap: %dMB/%dMB", usedHeap, maxHeap);
            }
            
        } catch (Exception e) {
            return "Memory: N/A";
        }
    }
    
    /**
     * Attempts to get total system memory using various methods
     */
    private long getTotalSystemMemory() {
        try {
            // Try using com.sun.management.OperatingSystemMXBean if available
            var osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                long totalMem = sunOsBean.getTotalMemorySize();
                if (totalMem > 0) {
                    return totalMem / (1024 * 1024); // Convert to MB
                }
            }
        } catch (Exception ignored) {}
        
        try {
            // Fallback: estimate based on Runtime max memory
            long maxHeap = Runtime.getRuntime().maxMemory() / (1024 * 1024);
            // Rough estimate: assume system has at least 4x the max heap size
            return Math.max(maxHeap * 4, 4096); // At least 4GB
        } catch (Exception ignored) {}
        
        return 0;
    }
    
    /**
     * Attempts to get used system memory
     */
    private long getUsedSystemMemory() {
        try {
            var osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                long totalMem = sunOsBean.getTotalMemorySize();
                long freeMem = sunOsBean.getFreeMemorySize();
                if (totalMem > 0 && freeMem >= 0) {
                    return (totalMem - freeMem) / (1024 * 1024); // Convert to MB
                }
            }
        } catch (Exception ignored) {}
        
        try {
            // Fallback: use Runtime memory info
            Runtime runtime = Runtime.getRuntime();
            long totalMem = runtime.totalMemory() / (1024 * 1024);
            long freeMem = runtime.freeMemory() / (1024 * 1024);
            return totalMem - freeMem;
        } catch (Exception ignored) {}
        
        return 0;
    }
}
