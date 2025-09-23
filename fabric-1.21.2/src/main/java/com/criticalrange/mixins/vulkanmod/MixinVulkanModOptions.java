package com.criticalrange.mixins.vulkanmod;

import com.criticalrange.util.MonitorInfoUtil;
import net.minecraft.text.Text;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.SwitchOption;
import net.vulkanmod.config.option.CyclingOption;
import net.vulkanmod.config.option.Options;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mixin to add VulkanMod Extra options to VulkanMod's Options system
 * Based on the working implementation from commit 010cf98
 */
@Mixin(value = Options.class, remap = false)
public class MixinVulkanModOptions {

    /**
     * Inject VulkanMod Extra option pages into video options
     */
    @Inject(
        method = "getVideoOpts",
        at = @At("RETURN"),
        remap = false,
        cancellable = true
    )
    private static void vulkanmodExtra$addExtraOptions(CallbackInfoReturnable<net.vulkanmod.config.gui.OptionBlock[]> cir) {
        try {

            // Get original blocks
            net.vulkanmod.config.gui.OptionBlock[] originalBlocks = cir.getReturnValue();
            List<net.vulkanmod.config.gui.OptionBlock> newBlocks = new ArrayList<>();

            // Add original blocks
            for (net.vulkanmod.config.gui.OptionBlock block : originalBlocks) {
                newBlocks.add(block);
            }

            // Add VulkanMod Extra block
            net.vulkanmod.config.gui.OptionBlock extraBlock = createVulkanModExtraBlock();
            if (extraBlock != null) {
                newBlocks.add(extraBlock);
            }

            // Set the modified blocks
            net.vulkanmod.config.gui.OptionBlock[] finalBlocks = newBlocks.toArray(new net.vulkanmod.config.gui.OptionBlock[0]);
            cir.setReturnValue(finalBlocks);

        } catch (Exception e) {
            // Silently ignore injection failures
        }
    }

    /**
     * Create VulkanMod Extra options block
     */
    @Unique
    private static net.vulkanmod.config.gui.OptionBlock createVulkanModExtraBlock() {
        try {
            List<Option<?>> options = new ArrayList<>();

            // Check if our config is available
            if (com.criticalrange.VulkanModExtra.CONFIG == null) {
                options.add(new SwitchOption(
                    Text.literal("VulkanMod Extra (Config Loading...)"),
                    value -> {},
                    () -> false
                ));
                return new net.vulkanmod.config.gui.OptionBlock(
                    "VulkanMod Extra",
                    options.toArray(new Option<?>[0])
                );
            }


            // Monitor selection - always show
            try {
                // Initialize monitors and get monitor list
                MonitorInfoUtil.initialize();
                List<MonitorInfoUtil.MonitorInfo> monitors = MonitorInfoUtil.getMonitors();

                String[] monitorNames;
                if (monitors.isEmpty()) {
                    // Fallback if no monitors detected
                    monitorNames = new String[]{"Primary"};
                } else {
                    monitorNames = monitors.stream()
                        .map(monitor -> monitor.name)
                        .toArray(String[]::new);
                }

                options.add(new CyclingOption<>(
                    Text.literal("Fullscreen Monitor"),
                    monitorNames,
                    value -> {
                        // Find monitor index by name
                        for (int i = 0; i < monitorNames.length; i++) {
                            if (monitorNames[i].equals(value)) {
                                com.criticalrange.VulkanModExtra.CONFIG.extraSettings.fullscreenMonitor = i;
                                saveConfig();
                                break;
                            }
                        }
                    },
                    () -> {
                        int index = com.criticalrange.VulkanModExtra.CONFIG.extraSettings.fullscreenMonitor;
                        return (index >= 0 && index < monitorNames.length) ? monitorNames[index] : monitorNames[0];
                    }
                ));
            } catch (Exception e) {
                // Silently ignore monitor selection creation failures
            }

            return new net.vulkanmod.config.gui.OptionBlock(
                "VulkanMod Extra",
                options.toArray(new Option<?>[0])
            );

        } catch (Exception e) {
            return null;
        }
    }

    @Unique
    private static void saveConfig() {
        try {
            if (com.criticalrange.VulkanModExtra.configManager != null) {
                // Save the current in-memory config (with all user changes)
                com.criticalrange.VulkanModExtra.configManager.saveConfig();
            }
        } catch (Exception e) {
            // Silently ignore config save failures
        }
    }

    /**
     * Inject tooltips into Graphics options
     */
    @Inject(
        method = "getGraphicsOpts",
        at = @At("RETURN"),
        remap = false
    )
    private static void vulkanmodExtra$injectGraphicsTooltips(CallbackInfoReturnable<net.vulkanmod.config.gui.OptionBlock[]> cir) {
        try {
            net.vulkanmod.config.gui.OptionBlock[] blocks = cir.getReturnValue();
            injectGraphicsTooltips(blocks);
        } catch (Exception e) {
            // Silently ignore tooltip injection failures
        }
    }

    /**
     * Inject tooltips into Optimization options
     */
    @Inject(
        method = "getOptimizationOpts",
        at = @At("RETURN"),
        remap = false
    )
    private static void vulkanmodExtra$injectOptimizationTooltips(CallbackInfoReturnable<net.vulkanmod.config.gui.OptionBlock[]> cir) {
        try {
            net.vulkanmod.config.gui.OptionBlock[] blocks = cir.getReturnValue();
            // VulkanMod's optimization options already have tooltips, no need to inject
        } catch (Exception e) {
            // Silently ignore tooltip injection failures
        }
    }

    /**
     * Inject tooltips into Other options
     */
    @Inject(
        method = "getOtherOpts",
        at = @At("RETURN"),
        remap = false
    )
    private static void vulkanmodExtra$injectOtherTooltips(CallbackInfoReturnable<net.vulkanmod.config.gui.OptionBlock[]> cir) {
        try {
            net.vulkanmod.config.gui.OptionBlock[] blocks = cir.getReturnValue();
            injectOtherTooltips(blocks);
        } catch (Exception e) {
            // Silently ignore tooltip injection failures
        }
    }

    /**
     * Inject tooltips into Graphics options blocks
     */
    @Unique
    private static void injectGraphicsTooltips(net.vulkanmod.config.gui.OptionBlock[] blocks) {
        if (blocks == null || blocks.length == 0) return;

        try {
            // Graphics options are in blocks[0], blocks[1], and blocks[2]
            if (blocks.length > 0) {
                injectTooltipsIntoBlock(blocks[0], new String[]{
                    "vulkanmod.options.graphics.renderDistance.tooltip",        // Render Distance
                    "vulkanmod.options.graphics.simulationDistance.tooltip",    // Simulation Distance
                    "vulkanmod.options.graphics.prioritizeChunkUpdates.tooltip" // Prioritize Chunk Updates
                });
            }

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
    private static void injectOtherTooltips(net.vulkanmod.config.gui.OptionBlock[] blocks) {
        if (blocks == null || blocks.length == 0) return;

        try {
            // Other options are in a single block
            if (blocks.length > 0) {
                injectTooltipsIntoBlock(blocks[0], new String[]{
                    "vulkanmod.options.other.chunkBuilderThreads.tooltip", // Builder Threads (called builderThreads in VulkanMod)
                    null, // Frame Queue already has tooltip
                    null  // Device Selector already has tooltip
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
            net.vulkanmod.config.option.Option<?>[] options = (net.vulkanmod.config.option.Option<?>[]) optionsField.get(block);

            if (options == null) return;

            for (int i = 0; i < Math.min(options.length, tooltipKeys.length); i++) {
                net.vulkanmod.config.option.Option<?> option = options[i];
                String tooltipKey = tooltipKeys[i];

                if (option != null && tooltipKey != null) {
                    // Only add tooltip if the option doesn't already have one
                    if (option.getTooltip() == null) {
                        try {
                            net.minecraft.text.Text tooltip = net.minecraft.text.Text.translatable(tooltipKey);
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
}