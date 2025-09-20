package com.criticalrange.mixins.vulkanmod;

import com.criticalrange.util.MonitorInfoUtil;
import net.minecraft.text.Text;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.option.SwitchOption;
import net.vulkanmod.config.option.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Mixin to add monitor information to VulkanMod's Video options page
 * Injects monitor info before the Fullscreen Resolution option
 */
@Mixin(Options.class)
public class MixinVulkanModOptions {

    @Unique
    private static volatile boolean monitorInfoInitialized = false;

    /**
     * Inject monitor information into the video options array
     * This modifies the return value to include monitor info before existing options
     */
    @Inject(
        method = "getVideoOpts",
        at = @At("RETURN"),
        remap = false,
        cancellable = true
    )
    private static void vulkanmodExtra$injectMonitorInfo(CallbackInfoReturnable<OptionBlock[]> cir) {
        try {
            
            // Initialize monitor info if not already done
            if (!monitorInfoInitialized) {
                MonitorInfoUtil.initialize();
                monitorInfoInitialized = true;
            }

            // Always show monitor info if available
            if (MonitorInfoUtil.isAvailable()) {
                OptionBlock[] originalBlocks = cir.getReturnValue();
                List<OptionBlock> newBlocks = new ArrayList<>();
                
                // Add monitor info block first
                OptionBlock monitorBlock = createMonitorInfoBlock();
                if (monitorBlock != null) {
                    newBlocks.add(monitorBlock);
                }
                
                // Add original blocks
                for (OptionBlock block : originalBlocks) {
                    newBlocks.add(block);
                }
                
                // Set the modified return value
                OptionBlock[] finalBlocks = newBlocks.toArray(new OptionBlock[0]);
                cir.setReturnValue(finalBlocks);
                
                // Inject tooltips into the original video options (not monitor info)
                injectVideoTooltips(finalBlocks);
            } else {
                // No monitor info, but still inject tooltips
                injectVideoTooltips(cir.getReturnValue());
            }
        } catch (Exception e) {
            // Log error but continue with original blocks
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to inject monitor info into video options: {}", e.getMessage());
            }
        }
    }

    /**
     * Create the monitor information option block
     */
    @Unique
    private static OptionBlock createMonitorInfoBlock() {
        try {
            List<Option<?>> options = new ArrayList<>();

            // Add current monitor info
            MonitorInfoUtil.MonitorInfo primaryMonitor = MonitorInfoUtil.getPrimaryMonitor();
            if (primaryMonitor != null) {
                // Current monitor info
                options.add(createInfoOption(
                    Text.literal(String.format("Monitor: %dx%d@%dHz", 
                        primaryMonitor.width, primaryMonitor.height, primaryMonitor.refreshRate)),
                    "Current monitor resolution and refresh rate"
                ));

                options.add(createInfoOption(
                    Text.literal(String.format("Color: %d-bit, DPI: %.1f", 
                        primaryMonitor.colorDepth, primaryMonitor.dpi)),
                    "Monitor color depth and DPI scaling"
                ));
            }

            // Add GPU info if available
            MonitorInfoUtil.GPUInfo gpuInfo = MonitorInfoUtil.getGPU();
            if (gpuInfo != null) {
                options.add(createInfoOption(
                    Text.literal(String.format("GPU: %s %s", gpuInfo.vendor, gpuInfo.name)),
                    "Graphics card vendor and model"
                ));

                if (gpuInfo.vramTotal > 0) {
                    options.add(createInfoOption(
                        Text.literal(String.format("VRAM: %s/%s", 
                            formatBytes(gpuInfo.vramAvailable), formatBytes(gpuInfo.vramTotal))),
                        "Graphics memory usage"
                    ));
                }
            }

            // Add current window info
            String windowInfo = MonitorInfoUtil.getCurrentWindowInfo();
            if (!windowInfo.contains("unavailable")) {
                options.add(createInfoOption(
                    Text.literal(windowInfo),
                    "Current game window information"
                ));
            }

            // Only create block if we have options
            if (!options.isEmpty()) {
                return new OptionBlock("Monitor Information", options.toArray(new Option<?>[0]));
            }

        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to create monitor info block: {}", e.getMessage());
            }
        }

        return null;
    }

    /**
     * Create the optimization options block
     */
    @Unique
    private static OptionBlock createOptimizationBlock() {
        try {
            List<Option<?>> options = new ArrayList<>();

            // Get configuration instance
            com.criticalrange.config.VulkanModExtraConfig.OptimizationSettings optSettings =
                com.criticalrange.VulkanModExtra.CONFIG.optimizationSettings;

            // Buffer pool optimization
            options.add(createSwitchOption(
                "Buffer Pooling",
                "Reuse GPU buffers instead of recreating them to reduce allocation overhead",
                () -> optSettings.bufferPooling,
                (value) -> optSettings.bufferPooling = value
            ));

            options.add(createRangeOption(
                "Buffer Pool Size",
                "Maximum number of buffers to keep in the pool (MB)",
                16, 256, 16,
                () -> optSettings.bufferPoolSize,
                (value) -> optSettings.bufferPoolSize = value
            ));

            return new OptionBlock("Optimization Settings", options.toArray(new Option<?>[0]));

        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to create optimization block: {}", e.getMessage());
            }
        }

        return null;
    }

    /**
     * Create an info option that displays static text
     */
    @Unique
    private static Option<?> createInfoOption(Text text, String description) {
        return new SwitchOption(
            text,
            value -> {}, // No-op setter (info is read-only)
            () -> false  // Always false (info display only)
        ).setTooltip(Text.literal(description));
    }

    /**
     * Format bytes to human readable format
     */
    @Unique
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
     * Reset initialization flag (call this when display configuration changes)
     */
    @Unique
    private static void resetMonitorInfo() {
        monitorInfoInitialized = false;
        MonitorInfoUtil.reset();
    }

    /**
     * Inject tooltips into Graphics page options
     */
    @Inject(
        method = "getGraphicsOpts", 
        at = @At("RETURN"),
        remap = false
    )
    private static void vulkanmodExtra$injectGraphicsTooltips(CallbackInfoReturnable<OptionBlock[]> cir) {
        try {
            OptionBlock[] blocks = cir.getReturnValue();
            injectGraphicsTooltips(blocks);
        } catch (Exception e) {
            // Silently continue on failure
        }
    }

    /**
     * Inject optimization options into Optimization page
     */
    @Inject(
        method = "getOptimizationOpts",
        at = @At("RETURN"),
        remap = false,
        cancellable = true
    )
    private static void vulkanmodExtra$injectOptimizationOptions(CallbackInfoReturnable<OptionBlock[]> cir) {
        try {
            OptionBlock[] originalBlocks = cir.getReturnValue();
            List<OptionBlock> newBlocks = new ArrayList<>();

            // Add original blocks first
            for (OptionBlock block : originalBlocks) {
                newBlocks.add(block);
            }

            // Add optimization options block after original options
            OptionBlock optimizationBlock = createOptimizationBlock();
            if (optimizationBlock != null) {
                newBlocks.add(optimizationBlock);
            }

            // Set the modified return value
            OptionBlock[] finalBlocks = newBlocks.toArray(new OptionBlock[0]);
            cir.setReturnValue(finalBlocks);
        } catch (Exception e) {
            // Silently continue on failure
        }
    }

    /**
     * Inject tooltips into Video options blocks
     */
    @Unique
    private static void injectVideoTooltips(OptionBlock[] blocks) {
        if (blocks == null || blocks.length == 0) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.warn("injectVideoTooltips: blocks are null or empty");
            }
            return;
        }

        try {
            // Find the actual video option blocks (skip monitor info if present)
            int startIndex = 0;
            
            // Skip monitor info block if it exists
            if (blocks.length > 0 && blocks[0] != null && 
                "Monitor Information".equals(getBlockTitle(blocks[0]))) {
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
     * Inject tooltips into Graphics options blocks
     */
    @Unique
    private static void injectGraphicsTooltips(OptionBlock[] blocks) {
        if (blocks == null || blocks.length == 0) return;

        try {
            // Graphics options are in blocks[0], blocks[1], and blocks[2]
            injectTooltipsIntoBlock(blocks[0], new String[]{
                "vulkanmod.options.graphics.renderDistance.tooltip",        // Render Distance
                "vulkanmod.options.graphics.simulationDistance.tooltip",    // Simulation Distance
                "vulkanmod.options.graphics.prioritizeChunkUpdates.tooltip" // Prioritize Chunk Updates
            });

            if (blocks.length > 1) {
                injectTooltipsIntoBlock(blocks[1], new String[]{
                    "vulkanmod.options.graphics.graphics.tooltip",          // Graphics
                    "vulkanmod.options.graphics.particles.tooltip",         // Particles
                    "vulkanmod.options.graphics.renderClouds.tooltip",      // Render Clouds
                    null, // Skip AO - already has tooltip
                    "vulkanmod.options.graphics.biomeBlendRadius.tooltip"   // Biome Blend Radius
                });
            }

            if (blocks.length > 2) {
                injectTooltipsIntoBlock(blocks[2], new String[]{
                    "vulkanmod.options.graphics.entityShadows.tooltip",         // Entity Shadows
                    "vulkanmod.options.graphics.entityDistanceScaling.tooltip", // Entity Distance Scaling
                    "vulkanmod.options.graphics.mipmapLevels.tooltip"           // Mipmap Levels
                });
            }


        } catch (Exception e) {
            // Silently continue on failure
        }
    }

    /**
     * Inject tooltips into Other options blocks
     */
    @Unique
    private static void injectOtherTooltips(OptionBlock[] blocks) {
        if (blocks == null || blocks.length == 0) return;

        try {
            // Other options are typically in a single block
            if (blocks.length > 0) {
                injectCustomOtherTooltips(blocks[0]);
            }


        } catch (Exception e) {
            // Silently continue on failure
        }
    }

    /**
     * Custom tooltip injection for Other options (Frame Queue Size, Device Selector, Chunk Builder Threads)
     */
    @Unique
    private static void injectCustomOtherTooltips(OptionBlock block) {
        try {
            if (block == null) return;

            // Access the options array through reflection
            java.lang.reflect.Field optionsField = OptionBlock.class.getDeclaredField("options");
            optionsField.setAccessible(true);
            Option<?>[] options = (Option<?>[]) optionsField.get(block);

            if (options == null) return;

            for (int i = 0; i < options.length; i++) {
                Option<?> option = options[i];
                if (option == null) continue;

                String optionName = option.getName().getString().toLowerCase();
                
                // Frame Queue Size tooltip
                if (i == 0 && optionName.contains("frame") && optionName.contains("queue")) {
                    if (option.getTooltip() == null) {
                        try {
                            Text tooltip = Text.translatable("vulkanmod.options.other.frameQueue.tooltip");
                            option.setTooltip(tooltip);
                        } catch (Exception e) {
                            // Silently continue on failure
                        }
                    }
                }
                
                // Device Selector tooltip with device name
                else if (i == 1 && optionName.contains("device")) {
                    if (option.getTooltip() == null) {
                        try {
                            String deviceName = getCurrentDeviceName();
                            Text tooltip = Text.literal(String.format("Current device: %s", deviceName));
                            option.setTooltip(tooltip);
                        } catch (Exception e) {
                            // Silently continue on failure
                        }
                    }
                }
                
                // Chunk Builder Threads tooltip
                else if (i == 2 && optionName.contains("chunk") && optionName.contains("thread")) {
                    if (option.getTooltip() == null) {
                        try {
                            Text tooltip = Text.translatable("vulkanmod.options.other.chunkBuilderThreads.tooltip");
                            option.setTooltip(tooltip);
                        } catch (Exception e) {
                            // Silently continue on failure
                        }
                    }
                }
            }

        } catch (Exception e) {
            // Silently continue on failure
        }
    }

    /**
     * Get current VulkanMod device name
     */
    @Unique
    private static String getCurrentDeviceName() {
        try {
            // Try to get device name from VulkanMod's DeviceManager
            Class<?> deviceManagerClass = Class.forName("net.vulkanmod.vulkan.device.DeviceManager");
            Object device = deviceManagerClass.getField("device").get(null);
            
            if (device != null) {
                String deviceName = (String) device.getClass().getField("deviceName").get(device);
                String vendor = (String) device.getClass().getField("vendorIdString").get(device);
                return vendor + " " + deviceName;
            }
        } catch (Exception e) {
            // VulkanMod not available or device not initialized
        }
        
        return "Unknown Device";
    }

    /**
     * Helper method to inject tooltips into an OptionBlock
     */
    @Unique
    private static void injectTooltipsIntoBlock(OptionBlock block, String[] tooltipKeys) {
        try {
            if (block == null) return;

            // Access the options array through reflection
            java.lang.reflect.Field optionsField = OptionBlock.class.getDeclaredField("options");
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
            // Silently continue on failure
        }
    }

    /**
     * Get block title safely
     */
    @Unique
    private static String getBlockTitle(OptionBlock block) {
        try {
            java.lang.reflect.Field titleField = OptionBlock.class.getDeclaredField("title");
            titleField.setAccessible(true);
            return (String) titleField.get(block);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Create a switch option with proper tooltip
     */
    @Unique
    private static Option<?> createSwitchOption(String name, String description, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return new SwitchOption(
            Text.literal(name),
            value -> {
                setter.accept(value);
                // Save configuration when options change
                // Use static reference for faster config save
                if (com.criticalrange.VulkanModExtra.configManager != null) {
                    com.criticalrange.VulkanModExtra.configManager.saveConfig();
                };
            },
            getter
        ).setTooltip(Text.literal(description));
    }

    /**
     * Create a range option with proper tooltip
     */
    @Unique
    private static Option<?> createRangeOption(String name, String description, int min, int max, int step,
                                               Supplier<Integer> getter, Consumer<Integer> setter) {
        return new net.vulkanmod.config.option.RangeOption(
            Text.literal(name),
            min, max, step,
            value -> {
                setter.accept(value);
                // Save configuration when options change
                // Use static reference for faster config save
                if (com.criticalrange.VulkanModExtra.configManager != null) {
                    com.criticalrange.VulkanModExtra.configManager.saveConfig();
                };
            },
            getter
        ).setTooltip(Text.literal(description));
    }

    /**
     * Create a custom range option with special handling for unlimited (0) value
     */
    @Unique
    private static Option<?> createCustomRangeOption(String name, String description, int min, int max, int step,
                                                     Supplier<Integer> getter, Consumer<Integer> setter) {
        return new net.vulkanmod.config.option.RangeOption(
            Text.literal(name),
            min, max, step,
            value -> {
                setter.accept(value);
                // Save configuration when options change
                // Use static reference for faster config save
                if (com.criticalrange.VulkanModExtra.configManager != null) {
                    com.criticalrange.VulkanModExtra.configManager.saveConfig();
                };
            },
            getter
        ) {
            @Override
            public Text getName() {
                int value = getter.get();
                if (value == 0) {
                    return Text.literal(name + ": None");
                }
                return Text.literal(name + ": " + value);
            }
        }.setTooltip(Text.literal(description));
    }
}