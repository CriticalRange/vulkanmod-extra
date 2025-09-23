package com.criticalrange.util;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.MinecraftClient;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for gathering monitor and system information
 * Provides comprehensive display, GPU, and system details for debugging and optimization
 */
public class MonitorInfoUtil {
    
    private static volatile SystemInfo systemInfo = null;
    private static volatile boolean initialized = false;
    private static volatile List<MonitorInfo> monitorInfos = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static volatile GPUInfo gpuInfo = null;
    private static volatile SystemInfoData systemInfoData = null;
    private static volatile boolean isShuttingDown = false;

    // Static initializer to register shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(MonitorInfoUtil::shutdown, "MonitorInfoUtil-Shutdown"));
    }
    
    /**
     * Monitor information structure
     */
    public static class MonitorInfo {
        public final String name;
        public final String realModelName;
        public final String manufacturer;
        public final String serialNumber;
        public final int width;
        public final int height;
        public final int refreshRate;
        public final int colorDepth;
        public final double dpi;
        public final boolean primary;
        public final String bounds;
        
        
        // Constructor for OSHI-based monitor info (when AWT is not available)
        public MonitorInfo(String name, String realModelName, String manufacturer, String serialNumber,
                         int width, int height, int refreshRate, int colorDepth, double dpi, 
                         boolean primary, String bounds) {
            this.name = name;
            this.realModelName = realModelName != null ? realModelName : "Unknown Model";
            this.manufacturer = manufacturer != null ? manufacturer : "Unknown";
            this.serialNumber = serialNumber != null ? serialNumber : "";
            this.width = width;
            this.height = height;
            this.refreshRate = refreshRate;
            this.colorDepth = colorDepth;
            this.dpi = dpi;
            this.primary = primary;
            this.bounds = bounds;
        }
        
        
        @Override
        public String toString() {
            return String.format("%s (%s) - %dx%d@%dHz, %d-bit, %.1f DPI", 
                realModelName, primary ? "Primary" : "Secondary", 
                width, height, refreshRate, colorDepth, dpi);
        }
    }
    
    /**
     * GPU information structure
     */
    public static class GPUInfo {
        public final String vendor;
        public final String name;
        public final String driverVersion;
        public final String vulkanVersion;
        public final long vramTotal;
        public final long vramAvailable;
        public final int vendorId;
        
        public GPUInfo(String vendor, String name, String driverVersion, String vulkanVersion, 
                     long vramTotal, long vramAvailable, int vendorId) {
            this.vendor = vendor;
            this.name = name;
            this.driverVersion = driverVersion;
            this.vulkanVersion = vulkanVersion;
            this.vramTotal = vramTotal;
            this.vramAvailable = vramAvailable;
            this.vendorId = vendorId;
        }
        
        @Override
        public String toString() {
            return String.format("%s %s (VRAM: %s/%s)", vendor, name, 
                formatBytes(vramAvailable), formatBytes(vramTotal));
        }
    }
    
    /**
     * System information structure
     */
    public static class SystemInfoData {
        public final String osName;
        public final String osVersion;
        public final String cpuName;
        public final int cpuCores;
        public final long totalMemory;
        public final long availableMemory;
        public final String javaVersion;
        public final String javaVM;
        
        public SystemInfoData(String osName, String osVersion, String cpuName, int cpuCores,
                            long totalMemory, long availableMemory, String javaVersion, String javaVM) {
            this.osName = osName;
            this.osVersion = osVersion;
            this.cpuName = cpuName;
            this.cpuCores = cpuCores;
            this.totalMemory = totalMemory;
            this.availableMemory = availableMemory;
            this.javaVersion = javaVersion;
            this.javaVM = javaVM;
        }
        
        @Override
        public String toString() {
            return String.format("%s %s, CPU: %s (%d cores), RAM: %s/%s", 
                osName, osVersion, cpuName, cpuCores,
                formatBytes(availableMemory), formatBytes(totalMemory));
        }
    }
    
    /**
     * Initialize monitor information gathering
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            // Initialize SystemInfo singleton
            if (systemInfo == null) {
                systemInfo = new SystemInfo();
            }

            // Get monitor information
            monitorInfos = getMonitorInfo();

            // Get GPU information from VulkanMod if available
            gpuInfo = getGPUInfo();

            // Get system information
            systemInfoData = getSystemInfo();

            initialized = true;

            if (systemInfoData != null) {
                VulkanModExtra.LOGGER.debug("System: {}", systemInfoData);
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to initialize monitor info: {}", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            initialized = false;
        }
    }
    
    /**
     * Get monitor information using OSHI with real model names from EDID
     */
    private static List<MonitorInfo> getMonitorInfo() {
        return getMonitorsFromOSHI();
    }
    
    /**
     * Get monitor information from OSHI with EDID parsing for real model names
     */
    private static List<MonitorInfo> getMonitorsFromOSHI() {
        List<MonitorInfo> infos = new ArrayList<>();

        try {
            // Use shared SystemInfo instance to prevent memory leaks
            if (systemInfo == null) {
                systemInfo = new SystemInfo();
            }
            List<oshi.hardware.Display> displays = systemInfo.getHardware().getDisplays();
            
            for (int i = 0; i < displays.size(); i++) {
                try {
                    oshi.hardware.Display display = displays.get(i);
                    
                    // Get display properties
                    byte[] edid = display.getEdid();
                    String modelName = extractModelNameFromEDID(edid);
                    String manufacturer = extractManufacturerFromEDID(edid);
                    String serialNumber = extractSerialFromEDID(edid);
                    
                    // Create monitor info from OSHI data
                    MonitorInfo info = createMonitorInfoFromOSHI(i, modelName, manufacturer, serialNumber);
                    infos.add(info);
                    
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.warn("Failed to process display {}: {}", i, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                }
            }
            
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("OSHI monitor detection failed: {}", e.getMessage());
        }
        
        return infos;
    }
    
    /**
     * Extract real model name from EDID data
     */
    private static String extractModelNameFromEDID(byte[] edid) {
        if (edid == null || edid.length < 128) {
            return null;
        }
        
        // EDID parsing for monitor name (Descriptor Block 1: 0x36-0x47)
        // Look for Display Name descriptor (0xFC)
        for (int i = 54; i <= 108; i += 18) {
            if (i + 3 < edid.length && 
                edid[i] == 0 && edid[i + 1] == 0 && edid[i + 2] == 0 && 
                edid[i + 3] == (byte) 0xFC) {
                
                // Found monitor name descriptor
                StringBuilder name = new StringBuilder();
                for (int j = 5; j < 18; j++) {
                    if (i + j < edid.length) {
                        char c = (char) edid[i + j];
                        if (c == '\n' || c == '\r' || c == 0) {
                            break;
                        }
                        name.append(c);
                    }
                }
                
                String result = name.toString().trim();
                if (!result.isEmpty() && !result.contains("Generic")) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Extract manufacturer from EDID data
     */
    private static String extractManufacturerFromEDID(byte[] edid) {
        if (edid == null || edid.length < 8) {
            return null;
        }
        
        try {
            // Manufacturer ID is in bytes 8-9 (big endian)
            int manufacturerId = ((edid[8] & 0x7F) << 8) | (edid[9] & 0xFF);
            
            // Convert to 3-character manufacturer code
            char[] manufacturerCode = new char[3];
            manufacturerCode[0] = (char) (((manufacturerId >> 10) & 0x1F) + 0x40);
            manufacturerCode[1] = (char) (((manufacturerId >> 5) & 0x1F) + 0x40);
            manufacturerCode[2] = (char) ((manufacturerId & 0x1F) + 0x40);
            
            return new String(manufacturerCode);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract serial number from EDID data
     */
    private static String extractSerialFromEDID(byte[] edid) {
        if (edid == null || edid.length < 16) {
            return null;
        }
        
        try {
            // Serial number is in bytes 12-15 (little endian)
            int serial = ((edid[15] & 0xFF) << 24) | 
                        ((edid[14] & 0xFF) << 16) | 
                        ((edid[13] & 0xFF) << 8) | 
                        (edid[12] & 0xFF);
            
            if (serial != 0) {
                return String.valueOf(serial);
            }
        } catch (Exception e) {
            // Ignore and return null
        }
        
        return null;
    }
    
    
    /**
     * Create monitor info from OSHI data
     */
    private static MonitorInfo createMonitorInfoFromOSHI(int displayIndex, 
                                                         String modelName, String manufacturer, String serialNumber) {
        // Use default values since OSHI Display doesn't provide dimensions
        int width = 1920; // Default fallback
        int height = 1080; // Default fallback
        int refreshRate = 60;
        int colorDepth = 24;
        
        return new MonitorInfo(
            "Display " + displayIndex, // name
            modelName != null ? modelName : "Unknown Model", // realModelName
            manufacturer != null ? manufacturer : "Unknown", // manufacturer
            serialNumber, // serialNumber
            width, // width
            height, // height
            refreshRate, // refreshRate
            colorDepth, // colorDepth
            96.0, // dpi
            displayIndex == 0, // primary
            String.format("%dx%d @ 0,0", width, height) // bounds
        );
    }
    
    /**
     * Get GPU information from VulkanMod if available
     */
    private static GPUInfo getGPUInfo() {
        // Skip VulkanMod GPU detection during initialization to prevent crashes
        // VulkanMod may not be fully initialized when this is called
        return null;
    }
    
    /**
     * Get system information
     */
    private static SystemInfoData getSystemInfo() {
        try {
            OperatingSystem os = systemInfo.getOperatingSystem();
            CentralProcessor cpu = systemInfo.getHardware().getProcessor();
            GlobalMemory memory = systemInfo.getHardware().getMemory();
            
            String osName = os.toString();
            String osVersion = os.getVersionInfo().toString();
            String cpuName = cpu.getProcessorIdentifier().getName();
            int cpuCores = cpu.getLogicalProcessorCount();
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            String javaVersion = System.getProperty("java.version");
            String javaVM = System.getProperty("java.vm.name");
            
            return new SystemInfoData(osName, osVersion, cpuName, cpuCores,
                                    totalMemory, availableMemory, javaVersion, javaVM);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to get system info: {}", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            return null;
        }
    }
    
    /**
     * Format bytes to human readable format
     */
    private static String formatBytes(long bytes) {
        if (bytes == -1) return "Unknown";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    /**
     * Get all monitor information
     */
    public static List<MonitorInfo> getMonitors() {
        if (!initialized) initialize();
        return new ArrayList<>(monitorInfos);
    }
    
    /**
     * Get primary monitor information
     */
    public static MonitorInfo getPrimaryMonitor() {
        if (!initialized) initialize();
        return monitorInfos.stream()
            .filter(monitor -> monitor.primary)
            .findFirst()
            .orElse(monitorInfos.isEmpty() ? null : monitorInfos.get(0));
    }
    
    /**
     * Get GPU information
     */
    public static GPUInfo getGPU() {
        if (!initialized) initialize();
        return gpuInfo;
    }
    
    /**
     * Get system information
     */
    public static SystemInfoData getSystemInfoData() {
        if (!initialized) initialize();
        return systemInfoData;
    }
    
    /**
     * Get current window information
     */
    public static String getCurrentWindowInfo() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return "Window not available";
        }
        
        try {
            int width = mc.getWindow().getWidth();
            int height = mc.getWindow().getHeight();
            int scaledWidth = mc.getWindow().getScaledWidth();
            int scaledHeight = mc.getWindow().getScaledHeight();
            double scaleFactor = mc.getWindow().getScaleFactor();
            
            return String.format("Window: %dx%d (scaled: %dx%d, scale: %.2fx)", 
                width, height, scaledWidth, scaledHeight, scaleFactor);
        } catch (Exception e) {
            return "Window info unavailable";
        }
    }
    
    /**
     * Generate comprehensive system report for debugging
     */
    public static String generateSystemReport() {
        if (!initialized) initialize();
        
        StringBuilder report = new StringBuilder();
        report.append("=== VulkanMod Extra System Report ===\n\n");
        
        // System Info
        if (systemInfoData != null) {
            report.append("System:\n");
            report.append("  OS: ").append(systemInfoData.osName).append(" ").append(systemInfoData.osVersion).append("\n");
            report.append("  CPU: ").append(systemInfoData.cpuName).append(" (").append(systemInfoData.cpuCores).append(" cores)\n");
            report.append("  RAM: ").append(formatBytes(systemInfoData.availableMemory)).append(" / ").append(formatBytes(systemInfoData.totalMemory)).append("\n");
            report.append("  Java: ").append(systemInfoData.javaVersion).append(" (").append(systemInfoData.javaVM).append(")\n");
        }
        
        report.append("\n");
        
        // GPU Info
        if (gpuInfo != null) {
            report.append("GPU:\n");
            report.append("  Vendor: ").append(gpuInfo.vendor).append("\n");
            report.append("  Name: ").append(gpuInfo.name).append("\n");
            report.append("  Driver: ").append(gpuInfo.driverVersion).append("\n");
            report.append("  Vulkan: ").append(gpuInfo.vulkanVersion).append("\n");
            report.append("  VRAM: ").append(formatBytes(gpuInfo.vramAvailable)).append(" / ").append(formatBytes(gpuInfo.vramTotal)).append("\n");
        }
        
        report.append("\n");
        
        // Monitor Info
        report.append("Monitors:\n");
        for (MonitorInfo monitor : monitorInfos) {
            report.append("  ").append(monitor.toString()).append("\n");
        }
        
        // Window Info
        report.append("\n");
        report.append(getCurrentWindowInfo()).append("\n");
        
        return report.toString();
    }
    
    /**
     * Check if monitor info is available
     */
    public static boolean isAvailable() {
        return initialized;
    }
    
    /**
     * Reset and re-initialize (useful if display configuration changes)
     */
    public static synchronized void reset() {
        initialized = false;
        monitorInfos.clear();
        gpuInfo = null;
        systemInfoData = null;
        initialize();
    }

    /**
     * Cleanup resources to prevent memory leaks
     */
    public static synchronized void cleanup() {
        initialized = false;
        monitorInfos.clear();
        gpuInfo = null;
        systemInfoData = null;
        systemInfo = null; // Release OSHI resources
    }

    /**
     * Shutdown hook to ensure cleanup during JVM shutdown
     */
    public static void shutdown() {
        isShuttingDown = true;
        try {
            cleanup();
            VulkanModExtra.LOGGER.info("MonitorInfoUtil shutdown completed");
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Error during MonitorInfoUtil shutdown", e);
        }
    }

    /**
     * Check if the utility is shutting down
     * @return true if shutting down
     */
    public static boolean isShuttingDown() {
        return isShuttingDown;
    }
}