package com.criticalrange.features.monitor;

import com.criticalrange.core.BaseFeature;
import com.criticalrange.core.FeatureCategory;
import com.criticalrange.util.MonitorInfoUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Monitor Information feature - adds display and system information to debug screen
 * Provides detailed monitor, GPU, and system information for troubleshooting and optimization
 */
public class MonitorInfoFeature extends BaseFeature {
    
    public MonitorInfoFeature() {
        super("monitor_info", "Monitor Info", FeatureCategory.PERFORMANCE,
              "Add monitor and system information to debug screen");
        
        // Initialize monitor info on feature creation
        MonitorInfoUtil.initialize();
    }
    
    /**
     * Check if monitor information is available
     */
    public boolean isAvailable() {
        return MonitorInfoUtil.isAvailable();
    }
    
    /**
     * Get monitor information for display
     */
    public String getMonitorDisplayInfo() {
        if (!isAvailable()) {
            return "Monitor info unavailable";
        }
        
        MonitorInfoUtil.MonitorInfo primary = MonitorInfoUtil.getPrimaryMonitor();
        if (primary != null) {
            return String.format("Monitor: %dx%d@%dHz, %d-bit, %.1f DPI", 
                primary.width, primary.height, primary.refreshRate, 
                primary.colorDepth, primary.dpi);
        }
        
        return "No monitor info";
    }
    
    /**
     * Get GPU information for display
     */
    public String getGPUDisplayInfo() {
        if (!isAvailable()) {
            return "GPU info unavailable";
        }
        
        MonitorInfoUtil.GPUInfo gpu = MonitorInfoUtil.getGPU();
        if (gpu != null) {
            return String.format("GPU: %s %s (%s/%s)", 
                gpu.vendor, gpu.name, 
                formatBytes(gpu.vramAvailable), formatBytes(gpu.vramTotal));
        }
        
        return "GPU info unavailable";
    }
    
    /**
     * Get system information for display
     */
    public String getSystemDisplayInfo() {
        if (!isAvailable()) {
            return "System info unavailable";
        }
        
        MonitorInfoUtil.SystemInfoData system = MonitorInfoUtil.getSystemInfoData();
        if (system != null) {
            return String.format("System: %s (%d cores), RAM: %s/%s", 
                system.cpuName, system.cpuCores,
                formatBytes(system.availableMemory), formatBytes(system.totalMemory));
        }
        
        return "System info unavailable";
    }
    
    /**
     * Format bytes to human readable format
     */
    private String formatBytes(long bytes) {
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
     * Mixin to add monitor info to debug screen
     */
    @Mixin(DebugHud.class)
    public static class DebugHudMixin {
        
        @Inject(method = "getLeftText", at = @At("RETURN"))
        private void vulkanmodExtra$addMonitorInfo(CallbackInfo ci) {
            try {
                // This would be called by the debug screen to add our info
                // Implementation would be handled by the feature system
            } catch (Exception e) {
                // Ignore errors in debug screen injection
            }
        }
    }
    
    /**
     * Generate comprehensive system report
     */
    public String generateSystemReport() {
        return MonitorInfoUtil.generateSystemReport();
    }
    
    /**
     * Reset and re-initialize monitor information
     */
    public void reset() {
        MonitorInfoUtil.reset();
    }
}