package com.criticalrange.util;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.MinecraftClient;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for gathering monitor and system information
 * Provides comprehensive display, GPU, and system details for debugging and optimization
 */
public class MonitorInfoUtil {
    
    private static final SystemInfo systemInfo = new SystemInfo();
    private static volatile boolean initialized = false;
    private static volatile List<MonitorInfo> monitorInfos = new ArrayList<>();
    private static volatile GPUInfo gpuInfo = null;
    private static volatile SystemInfoData systemInfoData = null;
    
    /**
     * Monitor information structure
     */
    public static class MonitorInfo {
        public final String name;
        public final int width;
        public final int height;
        public final int refreshRate;
        public final int colorDepth;
        public final double dpi;
        public final boolean primary;
        public final String bounds;
        
        public MonitorInfo(GraphicsDevice device, GraphicsConfiguration config) {
            this.name = device.getIDstring();
            DisplayMode mode = device.getDisplayMode();
            this.width = mode.getWidth();
            this.height = mode.getHeight();
            this.refreshRate = mode.getRefreshRate();
            this.colorDepth = mode.getBitDepth();
            this.dpi = getDPI(config);
            this.primary = device == GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            this.bounds = getBounds(config);
        }
        
        private double getDPI(GraphicsConfiguration config) {
            try {
                return config.getDefaultTransform().getScaleX() * 96.0;
            } catch (Exception e) {
                return 96.0; // Default DPI
            }
        }
        
        private String getBounds(GraphicsConfiguration config) {
            Rectangle bounds = config.getBounds();
            return String.format("%dx%d @ %d,%d", bounds.width, bounds.height, bounds.x, bounds.y);
        }
        
        @Override
        public String toString() {
            return String.format("%s (%s) - %dx%d@%dHz, %d-bit, %.1f DPI", 
                name, primary ? "Primary" : "Secondary", 
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
            // Get monitor information
            monitorInfos = getMonitorInfo();
            
            // Get GPU information from VulkanMod if available
            gpuInfo = getGPUInfo();
            
            // Get system information
            systemInfoData = getSystemInfo();
            
            initialized = true;
            
            VulkanModExtra.LOGGER.info("Monitor info initialized: {} monitors detected", monitorInfos.size());
            for (MonitorInfo monitor : monitorInfos) {
                VulkanModExtra.LOGGER.debug("Monitor: {}", monitor);
            }
            if (gpuInfo != null) {
                VulkanModExtra.LOGGER.debug("GPU: {}", gpuInfo);
            }
            if (systemInfoData != null) {
                VulkanModExtra.LOGGER.debug("System: {}", systemInfoData);
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to initialize monitor info: {}", e.getMessage());
            initialized = false;
        }
    }
    
    /**
     * Get monitor information from AWT
     */
    private static List<MonitorInfo> getMonitorInfo() {
        List<MonitorInfo> infos = new ArrayList<>();
        
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            
            for (GraphicsDevice screen : screens) {
                try {
                    GraphicsConfiguration gc = screen.getDefaultConfiguration();
                    MonitorInfo info = new MonitorInfo(screen, gc);
                    infos.add(info);
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.warn("Failed to get info for screen {}: {}", screen.getIDstring(), e.getMessage());
                }
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to get monitor information: {}", e.getMessage());
        }
        
        return infos;
    }
    
    /**
     * Get GPU information from VulkanMod if available
     */
    private static GPUInfo getGPUInfo() {
        try {
            // Try to get GPU info from VulkanMod's DeviceManager
            Object device = null;
            try {
                Class<?> deviceManagerClass = Class.forName("net.vulkanmod.vulkan.device.DeviceManager");
                device = deviceManagerClass.getField("device").get(null);
            } catch (Exception e) {
                // VulkanMod not available or device not initialized
                return null;
            }
            
            if (device != null) {
                String vendor = (String) device.getClass().getField("vendorIdString").get(device);
                String name = (String) device.getClass().getField("deviceName").get(device);
                String driverVersion = (String) device.getClass().getField("driverVersion").get(device);
                String vulkanVersion = (String) device.getClass().getField("vkVersion").get(device);
                int vendorId = device.getClass().getField("vendorId").getInt(device);
                
                // Get VRAM info from OSHI
                long vramTotal = 0;
                long vramAvailable = 0;
                try {
                    GlobalMemory memory = systemInfo.getHardware().getMemory();
                    vramTotal = memory.getVirtualMemory().getSwapTotal();
                    vramAvailable = memory.getVirtualMemory().getSwapUsed();
                    
                    // Fallback to GPU VRAM if available
                    for (oshi.hardware.GraphicsCard gpu : systemInfo.getHardware().getGraphicsCards()) {
                        if (gpu.getName().contains(name)) {
                            vramTotal = gpu.getVRam();
                            vramAvailable = vramTotal; // Use total as fallback since we don't have used memory
                            break;
                        }
                    }
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.warn("Could not get VRAM info: {}", e.getMessage());
                }
                
                return new GPUInfo(vendor, name, driverVersion, vulkanVersion, 
                                 vramTotal, vramAvailable, vendorId);
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Could not get GPU info from VulkanMod: {}", e.getMessage());
        }
        
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
            VulkanModExtra.LOGGER.error("Failed to get system info: {}", e.getMessage());
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
}