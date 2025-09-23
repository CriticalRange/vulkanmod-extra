package com.criticalrange.mixins.vulkanmod;

import com.criticalrange.util.MonitorInfoUtil;
import net.minecraft.text.Text;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.SwitchOption;
import net.vulkanmod.config.option.Options;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mixin to add monitor selection functionality to VulkanMod's video options
 * Extends the existing video options to support selecting different monitors for fullscreen
 */
@Mixin(Options.class)
public class MixinVulkanModMonitorSelection {

    @Unique
    private static volatile java.util.concurrent.CopyOnWriteArrayList<String> availableMonitors; // Thread-safe monitor names
    @Unique
    private static volatile java.util.concurrent.CopyOnWriteArrayList<Long> monitorHandles; // Thread-safe GLFW handles
    @Unique
    private static volatile int selectedMonitorIndex = 0;

    /**
     * Initialize monitor detection using OSHI for real monitor model names
     */
    @Unique
    private static void initializeMonitors() {
        if (availableMonitors == null) {
            availableMonitors = new java.util.concurrent.CopyOnWriteArrayList<>();
            monitorHandles = new java.util.concurrent.CopyOnWriteArrayList<>();
            try {
                // Use OSHI to get real monitor information
                List<MonitorInfoUtil.MonitorInfo> monitors = MonitorInfoUtil.getMonitors();
                
                if (monitors != null && !monitors.isEmpty()) {
                    for (int i = 0; i < monitors.size(); i++) {
                        MonitorInfoUtil.MonitorInfo monitor = monitors.get(i);
                        
                        // Use OSHI's real model name instead of GLFW generic name
                        String displayName;
                        if (monitor.realModelName != null && !monitor.realModelName.equals("Unknown Model")) {
                            displayName = monitor.primary ? 
                                monitor.realModelName + " (Primary)" : 
                                monitor.realModelName;
                        } else {
                            // Fallback to manufacturer + model combination
                            displayName = monitor.primary ? 
                                monitor.manufacturer + " " + monitor.realModelName + " (Primary)" : 
                                monitor.manufacturer + " " + monitor.realModelName;
                        }
                        
                        availableMonitors.add(displayName);
                        
                        // For monitor handles, we still need GLFW handles for VulkanMod compatibility
                        // Get GLFW monitor as fallback
                        try {
                            org.lwjgl.PointerBuffer glfwMonitors = org.lwjgl.glfw.GLFW.glfwGetMonitors();
                            if (glfwMonitors != null && i < glfwMonitors.limit()) {
                                monitorHandles.add(glfwMonitors.get(i));
                            } else {
                                monitorHandles.add(0L); // Fallback to primary
                            }
                        } catch (Exception e) {
                            monitorHandles.add(0L); // Fallback to primary
                        }
                    }
                } else {
                    // Fallback to GLFW if OSHI fails
                    initializeMonitorsWithGLFW();
                }
                
                // If we found monitors, set default to primary (index 0)
                if (!availableMonitors.isEmpty()) {
                    selectedMonitorIndex = 0;
                } else {
                    // Final fallback
                    availableMonitors.add("Primary");
                    monitorHandles.add(0L);
                    selectedMonitorIndex = 0;
                }
                
            } catch (Exception e) {
                // Fallback to GLFW if OSHI fails
                initializeMonitorsWithGLFW();
            }
        }
    }
    
    /**
     * Fallback monitor initialization using GLFW
     */
    @Unique
    private static void initializeMonitorsWithGLFW() {
        availableMonitors.clear();
        monitorHandles.clear();
        
        try {
            org.lwjgl.PointerBuffer monitors = org.lwjgl.glfw.GLFW.glfwGetMonitors();
            if (monitors != null && monitors.limit() > 0) {
                for (int i = 0; i < monitors.limit(); i++) {
                    long monitorHandle = monitors.get(i);
                    String monitorName = org.lwjgl.glfw.GLFW.glfwGetMonitorName(monitorHandle);
                    
                    if (monitorName != null && !monitorName.trim().isEmpty()) {
                        String displayName = i == 0 ? monitorName + " (Primary)" : monitorName;
                        availableMonitors.add(displayName);
                        monitorHandles.add(monitorHandle);
                    }
                }
            }
        } catch (Exception e) {
            // Final fallback
            availableMonitors.add("Primary");
            monitorHandles.add(0L);
        }
        
        selectedMonitorIndex = 0;
    }

    /**
     * Inject monitor selection option into video options
     */
    @Inject(
        method = "getVideoOpts",
        at = @At("RETURN"),
        remap = false,
        cancellable = true
    )
    private static void vulkanmodExtra$injectMonitorSelection(CallbackInfoReturnable<net.vulkanmod.config.gui.OptionBlock[]> cir) {
        try {
            
            initializeMonitors();
            
            // Find the "Fullscreen Resolution" block and add monitor selection to it
            net.vulkanmod.config.gui.OptionBlock[] originalBlocks = cir.getReturnValue();
            List<net.vulkanmod.config.gui.OptionBlock> newBlocks = new ArrayList<>();
            
            boolean monitorOptionAdded = false;
            
            // Look for the block containing fullscreen resolution
            for (net.vulkanmod.config.gui.OptionBlock block : originalBlocks) {
                // Check if this block contains fullscreen/resolution related options
                if (!monitorOptionAdded && containsResolutionOption(block)) {
                    // Add monitor selection to this existing block
                    net.vulkanmod.config.gui.OptionBlock modifiedBlock = addMonitorSelectionToBlock(block);
                    if (modifiedBlock != null) {
                        newBlocks.add(modifiedBlock);
                        monitorOptionAdded = true;
                    } else {
                        newBlocks.add(block); // Fallback to original block
                    }
                } else {
                    newBlocks.add(block);
                }
            }
            
            // If we couldn't add to existing block, create a new one as fallback
            if (!monitorOptionAdded) {
                net.vulkanmod.config.gui.OptionBlock monitorBlock = createMonitorSelectionBlock();
                if (monitorBlock != null) {
                    newBlocks.add(monitorBlock);
                }
            }
            
            // Set the modified blocks
            net.vulkanmod.config.gui.OptionBlock[] finalBlocks = newBlocks.toArray(new net.vulkanmod.config.gui.OptionBlock[0]);
            cir.setReturnValue(finalBlocks);
            
            // Also inject video tooltips into the final blocks
            injectVideoTooltips(finalBlocks);
        } catch (Exception e) {
            // Log error but continue with original blocks
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to inject monitor selection into video options: {}", e.getMessage());
            }
        }
    }

    /**
     * Check if an option block contains resolution-related options
     */
    @Unique
    private static boolean containsResolutionOption(net.vulkanmod.config.gui.OptionBlock block) {
        try {
            // Check the block title for resolution/fullscreen keywords
            String blockTitle = block.title().toLowerCase();
            if (blockTitle.contains("resolution") || blockTitle.contains("fullscreen")) {
                return true;
            }
            
            // Check individual options in the block for resolution-related names
            Option<?>[] options = block.options();
            for (Option<?> option : options) {
                String optionName = option.getName().getString().toLowerCase();
                if (optionName.contains("resolution") || optionName.contains("fullscreen") || optionName.contains("video mode")) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to check block for resolution options: {}", e.getMessage());
            }
            return false;
        }
    }

    /**
     * Add monitor selection option to an existing option block
     */
    @Unique
    private static net.vulkanmod.config.gui.OptionBlock addMonitorSelectionToBlock(net.vulkanmod.config.gui.OptionBlock originalBlock) {
        try {
            // Get existing options
            Option<?>[] originalOptions = originalBlock.options();
            List<Option<?>> newOptions = new ArrayList<>();
            
            // Add monitor selection option FIRST
            Option<?> monitorOption = createMonitorSelectionOption();
            if (monitorOption != null) {
                newOptions.add(monitorOption);
            }
            
            // Then add all original options
            for (Option<?> option : originalOptions) {
                newOptions.add(option);
            }
            
            // Create new block with same title but additional option
            return new net.vulkanmod.config.gui.OptionBlock(
                originalBlock.title(),
                newOptions.toArray(new Option<?>[0])
            );
            
        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to add monitor selection to existing block: {}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * Create just the monitor selection option (not a whole block)
     */
    @Unique
    private static Option<?> createMonitorSelectionOption() {
        try {
            // Create monitor names array for cycling option
            String[] monitorNames;
            if (availableMonitors.isEmpty()) {
                // Fallback if no monitors detected
                monitorNames = new String[]{"Primary"};
            } else {
                monitorNames = availableMonitors.toArray(new String[0]);
            }

            // Create monitor selection option
            Option<?> monitorOption = createCyclingOption(
                Text.translatable("vulkanmod-extra.option.video.monitorSelection"),
                monitorNames,
                (value) -> {
                    selectedMonitorIndex = value;
                    updateVideoModesForSelectedMonitor();
                },
                () -> selectedMonitorIndex
            );

            // Set the tooltip for the monitor selection option
            if (monitorOption != null) {
                try {
                    Text tooltip = Text.translatable("vulkanmod-extra.option.video.monitorSelection.tooltip");
                    monitorOption.setTooltip(tooltip);
                } catch (Exception e) {
                    // Silently continue if tooltip fails
                }
            }
            
            return monitorOption;

        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to create monitor selection option: {}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * Create the monitor selection option block (fallback)
     */
    @Unique
    private static net.vulkanmod.config.gui.OptionBlock createMonitorSelectionBlock() {
        try {
            List<Option<?>> options = new ArrayList<>();

            // Create monitor names array for cycling option
            String[] monitorNames;
            if (availableMonitors.isEmpty()) {
                // Fallback if no monitors detected
                monitorNames = new String[]{"Primary"};
            } else {
                monitorNames = availableMonitors.toArray(new String[0]);
            }

            // Create monitor selection option
            Option<?> monitorOption = createCyclingOption(
                Text.translatable("vulkanmod-extra.option.video.monitor_selection"),
                monitorNames,
                (value) -> {
                    selectedMonitorIndex = value;
                    updateVideoModesForSelectedMonitor();
                },
                () -> selectedMonitorIndex
            );

            // Set the tooltip for the monitor selection option
            if (monitorOption != null) {
                try {
                    Text tooltip = Text.translatable("vulkanmod.options.video.monitor_selection.tooltip");
                    monitorOption.setTooltip(tooltip);
                } catch (Exception e) {
                    // Silently continue if tooltip fails
                }
            }

            options.add(monitorOption);

            return new net.vulkanmod.config.gui.OptionBlock(
                "",
                options.toArray(new Option<?>[0])
            );

        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to create monitor selection block: {}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * Create a cycling option using VulkanMod's CyclingOption directly
     */
    @Unique
    private static Option<?> createCyclingOption(Text name, String[] values, 
            java.util.function.Consumer<Integer> setter, java.util.function.Supplier<Integer> getter) {
        try {
            // Create a consumer that works with the string value instead of index
            java.util.function.Consumer<String> stringConsumer = (selectedValue) -> {
                for (int i = 0; i < values.length; i++) {
                    if (values[i].equals(selectedValue)) {
                        setter.accept(i);
                        break;
                    }
                }
            };
            
            // Create a supplier that returns the string value
            java.util.function.Supplier<String> stringSupplier = () -> {
                int index = getter.get();
                if (index >= 0 && index < values.length) {
                    return values[index];
                }
                return values[0]; // Default to first option
            };
            
            // Use VulkanMod's CyclingOption directly with proper translator
            net.vulkanmod.config.option.CyclingOption<String> option = new net.vulkanmod.config.option.CyclingOption<>(
                name,
                values,
                stringConsumer,
                stringSupplier
            );
            
            // Set the translator function so VulkanMod knows how to display the value
            option.setTranslator(value -> net.minecraft.text.Text.literal(value)); // Convert string to Component
            
            return option;
            
        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to create cycling option: {}", e.getMessage());
            }
            // Fallback to a simple switch option
            return new SwitchOption(
                name,
                value -> {}, // No-op for now
                () -> false
            );
        }
    }

    /**
     * Update video modes when monitor selection changes
     */
    @Unique
    private static void updateVideoModesForSelectedMonitor() {
        try {
            if (selectedMonitorIndex >= 0 && selectedMonitorIndex < availableMonitors.size() && 
                selectedMonitorIndex < monitorHandles.size()) {
                
                String selectedMonitorName = availableMonitors.get(selectedMonitorIndex);
                long monitorHandle = monitorHandles.get(selectedMonitorIndex);
                
                if (monitorHandle != 0) {
                    // Update video modes using VulkanMod's VideoModeManager
                    VideoModeSet[] newVideoModes = VideoModeManager.populateVideoResolutions(monitorHandle);
                    
                    // Update the video mode sets in VideoModeManager using reflection
                    try {
                        java.lang.reflect.Field videoModeSetsField = VideoModeManager.class.getDeclaredField("videoModeSets");
                        videoModeSetsField.setAccessible(true);
                        videoModeSetsField.set(null, newVideoModes);
                        
                        // Also update the current video mode to match the selected monitor
                        VideoModeSet.VideoMode currentMode = VideoModeManager.getCurrentVideoMode(monitorHandle);
                        try {
                            java.lang.reflect.Field selectedVideoModeField = VideoModeManager.class.getDeclaredField("selectedVideoMode");
                            selectedVideoModeField.setAccessible(true);
                            selectedVideoModeField.set(null, currentMode);
                        } catch (Exception e) {
                            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to update selected video mode: {}", e.getMessage());
                            }
                        }
                        
                        
                    } catch (Exception e) {
                        if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                            com.criticalrange.VulkanModExtra.LOGGER.error("Failed to update video mode sets: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to update video modes for selected monitor: {}", e.getMessage());
            }
        }
    }

    /**
     * Reset monitor initialization (call when display configuration changes)
     */
    @Unique
    private static void resetMonitorSelection() {
        availableMonitors = null;
        monitorHandles = null;
        selectedMonitorIndex = 0;
        // Also reset OSHI cache to get fresh monitor information
        MonitorInfoUtil.reset();
    }

    /**
     * Cleanup monitor selection resources to prevent memory leaks
     */
    @Unique
    private static void cleanupMonitorSelection() {
        if (availableMonitors != null) {
            availableMonitors.clear();
            availableMonitors = null;
        }
        if (monitorHandles != null) {
            monitorHandles.clear();
            monitorHandles = null;
        }
        selectedMonitorIndex = 0;
        MonitorInfoUtil.cleanup();
    }

    /**
     * Inject tooltips into Video options blocks
     */
    @Unique
    private static void injectVideoTooltips(net.vulkanmod.config.gui.OptionBlock[] blocks) {
        if (blocks == null || blocks.length == 0) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.warn("injectVideoTooltips: blocks are null or empty");
            }
            return;
        }

        try {
            
            // Find the actual video option blocks (skip monitor info if present)
            int startIndex = 0;
            
            // Skip monitor info block if it exists (it would be the first block we added)
            if (blocks.length > 0 && blocks[0] != null && 
                blocks[0].title().contains("Monitor")) {
                startIndex = 1;
            }

            // Video options are usually in blocks[startIndex], blocks[startIndex+1], blocks[startIndex+2]
            if (blocks.length > startIndex) {
                injectTooltipsIntoBlock(blocks[startIndex], new String[]{
                    "vulkanmod.options.video.resolution.tooltip",        // Resolution
                    "vulkanmod.options.video.refreshRate.tooltip",       // Refresh Rate  
                    "vulkanmod.options.video.fullscreen.tooltip",        // Fullscreen
                    "vulkanmod.options.video.windowedFullscreen.tooltip", // Windowed Fullscreen
                    "vulkanmod.options.video.framerateLimit.tooltip",    // Framerate Limit
                    "vulkanmod.options.video.vsync.tooltip"              // VSync
                });
            }

            if (blocks.length > startIndex + 1) {
                injectTooltipsIntoBlock(blocks[startIndex + 1], new String[]{
                    "vulkanmod.options.video.guiScale.tooltip",      // GUI Scale
                    "vulkanmod.options.video.gamma.tooltip"          // Brightness (Gamma)
                });
            }

            if (blocks.length > startIndex + 2) {
                injectTooltipsIntoBlock(blocks[startIndex + 2], new String[]{
                    "vulkanmod.options.video.viewBobbing.tooltip",       // View Bobbing
                    "vulkanmod.options.video.attackIndicator.tooltip",   // Attack Indicator
                    "vulkanmod.options.video.autosaveIndicator.tooltip"  // Autosave Indicator
                });
            }


        } catch (Exception e) {
            // Silently continue on failure
        }
    }

    /**
     * Helper method to inject tooltips into an OptionBlock
     */
    @Unique
    private static void injectTooltipsIntoBlock(net.vulkanmod.config.gui.OptionBlock block, String[] tooltipKeys) {
        try {
            if (block == null) return;

            // Access the options array through reflection
            java.lang.reflect.Field optionsField = net.vulkanmod.config.gui.OptionBlock.class.getDeclaredField("options");
            optionsField.setAccessible(true);
            Option<?>[] options = (Option<?>[]) optionsField.get(block);

            if (options == null) return;

            for (int i = 0; i < Math.min(options.length, tooltipKeys.length); i++) {
                Option<?> option = options[i];
                String tooltipKey = tooltipKeys[i];

                if (option != null && tooltipKey != null) {
                    // Only add tooltip if the option doesn't already have one
                    if (option.getTooltip() == null) {
                        try {
                            Text tooltip = Text.translatable(tooltipKey);
                            option.setTooltip(tooltip);
                        } catch (Exception e) {
                            // Silently continue on failure
                        }
                    }
                }
            }

        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.warn("Failed to inject tooltips into option block", e);
            }
        }
    }
}