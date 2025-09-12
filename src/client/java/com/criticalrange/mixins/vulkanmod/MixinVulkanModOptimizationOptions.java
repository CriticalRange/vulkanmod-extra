package com.criticalrange.mixins.vulkanmod;

import net.minecraft.text.Text;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.SwitchOption;
import net.vulkanmod.config.option.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mixin to add VulkanMod Extra optimization options to VulkanMod's optimization page
 * Adds "Use Fast Random" and "Linear Flat Color Blender" to the optimization settings
 */
@Mixin(Options.class)
public class MixinVulkanModOptimizationOptions {

    /**
     * Inject optimization options into VulkanMod's optimization page
     */
    @Inject(
        method = "getOptimizationOpts",
        at = @At("RETURN"),
        remap = false,
        cancellable = true
    )
    private static void vulkanmodExtra$injectOptimizationOptions(CallbackInfoReturnable<net.vulkanmod.config.gui.OptionBlock[]> cir) {
        try {

            net.vulkanmod.config.gui.OptionBlock[] originalBlocks = cir.getReturnValue();
            List<net.vulkanmod.config.gui.OptionBlock> newBlocks = new ArrayList<>();

            // Add all original blocks first
            for (net.vulkanmod.config.gui.OptionBlock block : originalBlocks) {
                newBlocks.add(block);
            }

            // Add our "Extra Optimizations" block at the end
            net.vulkanmod.config.gui.OptionBlock extraOptimizationBlock = createExtraOptimizationBlock();
            if (extraOptimizationBlock != null) {
                newBlocks.add(extraOptimizationBlock);
            }

            cir.setReturnValue(newBlocks.toArray(new net.vulkanmod.config.gui.OptionBlock[0]));
            
        } catch (Exception e) {
            // Log error but continue with original blocks
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to inject optimization options: {}", e.getMessage());
            }
        }
    }


    /**
     * Create the Use Fast Random option
     */
    @Unique
    private static Option<?> createRandomOptimizationOption() {
        try {
            SwitchOption option = new SwitchOption(
                Text.translatable("vulkanmod-extra.option.extra.use_fast_random"),
                value -> {
                    com.criticalrange.VulkanModExtra.CONFIG.extraSettings.useRandomOptimization = value;
                    com.criticalrange.VulkanModExtra.CONFIG.writeChanges();
                },
                () -> com.criticalrange.VulkanModExtra.CONFIG.extraSettings.useRandomOptimization
            );

            // Set tooltip if available
            try {
                option.setTooltip(Text.translatable("vulkanmod-extra.option.extra.use_fast_random.tooltip"));
            } catch (Exception e) {
                // Tooltip is optional
            }

            return option;

        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to create fast random option: {}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * Create the Linear Flat Color Blender option
     */
    @Unique
    private static Option<?> createLinearColorBlenderOption() {
        try {
            SwitchOption option = new SwitchOption(
                Text.translatable("vulkanmod-extra.option.extra.linear_flat_color_blender"),
                value -> {
                    com.criticalrange.VulkanModExtra.CONFIG.extraSettings.linearFlatColorBlender = value;
                    com.criticalrange.VulkanModExtra.CONFIG.writeChanges();
                },
                () -> com.criticalrange.VulkanModExtra.CONFIG.extraSettings.linearFlatColorBlender
            );

            // Set tooltip if available
            try {
                option.setTooltip(Text.translatable("vulkanmod-extra.option.extra.linear_flat_color_blender.tooltip"));
            } catch (Exception e) {
                // Tooltip is optional
            }

            return option;

        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to create linear color blender option: {}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * Create "Extra Optimizations" option block
     */
    @Unique
    private static net.vulkanmod.config.gui.OptionBlock createExtraOptimizationBlock() {
        try {
            List<Option<?>> options = new ArrayList<>();

            Option<?> randomOptimizationOption = createRandomOptimizationOption();
            if (randomOptimizationOption != null) {
                options.add(randomOptimizationOption);
            }

            Option<?> linearColorBlenderOption = createLinearColorBlenderOption();
            if (linearColorBlenderOption != null) {
                options.add(linearColorBlenderOption);
            }

            if (options.isEmpty()) {
                return null;
            }

            return new net.vulkanmod.config.gui.OptionBlock(
                "",
                options.toArray(new Option<?>[0])
            );

        } catch (Exception e) {
            if (com.criticalrange.VulkanModExtra.LOGGER != null) {
                com.criticalrange.VulkanModExtra.LOGGER.error("Failed to create Extra Optimizations block: {}", e.getMessage());
            }
            return null;
        }
    }
}