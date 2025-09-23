package com.criticalrange.mixins.vulkanmod;

import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GraphicsMode;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.CyclingOption;
import net.vulkanmod.config.option.Options;
import net.vulkanmod.config.gui.OptionBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mixin to replace VulkanMod's limited graphics options with full vanilla options
 * Adds FABULOUS mode that VulkanMod excludes
 */
@Mixin(value = Options.class, remap = false)
public class MixinVulkanModGraphicsOptions {

    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod-Extra/FABULOUS");

    /**
     * Replace the graphics options to include FABULOUS mode
     */
    @Inject(
        method = "getGraphicsOpts",
        at = @At("RETURN"),
        remap = false,
        cancellable = true
    )
    private static void vulkanmodExtra$addFabulousGraphics(CallbackInfoReturnable<OptionBlock[]> cir) {
        try {
            OptionBlock[] originalBlocks = cir.getReturnValue();
            if (originalBlocks == null || originalBlocks.length < 2) {
                return; // Safety check
            }

            // Clone the original blocks
            OptionBlock[] newBlocks = new OptionBlock[originalBlocks.length];

            // Copy first block unchanged
            newBlocks[0] = originalBlocks[0];

            // Replace second block with enhanced graphics options
            newBlocks[1] = createEnhancedGraphicsBlock(originalBlocks[1]);

            // Copy remaining blocks unchanged
            for (int i = 2; i < originalBlocks.length; i++) {
                newBlocks[i] = originalBlocks[i];
            }

            cir.setReturnValue(newBlocks);

        } catch (Exception e) {
            // Silently fail to avoid breaking VulkanMod
        }
    }

    /**
     * Create enhanced graphics block with FABULOUS mode included
     */
    private static OptionBlock createEnhancedGraphicsBlock(OptionBlock originalBlock) {
        try {
            List<Option<?>> newOptions = new ArrayList<>();

            // Add enhanced graphics option with all three modes (OpenGL conflicts resolved via shader blocking)
            CyclingOption<GraphicsMode> enhancedGraphicsOption = new CyclingOption<>(
                Text.translatable("options.graphics"),
                GraphicsMode.values(), // Include FAST, FANCY, and FABULOUS (conflicts handled by MixinVulkanModTransparencyShader)
                value -> MinecraftClient.getInstance().options.getGraphicsMode().setValue(value),
                () -> MinecraftClient.getInstance().options.getGraphicsMode().getValue()
            );
            enhancedGraphicsOption.setTranslator(graphicsMode -> {
                if (graphicsMode == GraphicsMode.FABULOUS) {
                    // Custom formatting for FABULOUS mode with italic and experimental label
                    return Text.literal("§oFabulous! EXPERIMENTAL§r");
                } else {
                    // Use vanilla translations for FAST and FANCY
                    return Text.translatable(graphicsMode.getTranslationKey());
                }
            });

            // Add tooltip using our VulkanMod Extra translation system
            enhancedGraphicsOption.setTooltip(Text.translatable("vulkanmod.options.graphics.graphics.tooltip"));

            newOptions.add(enhancedGraphicsOption);

            // Get original options from the block and add the rest (except the first graphics option)
            Option<?>[] originalOptions = getOptionsFromBlock(originalBlock);
            if (originalOptions != null && originalOptions.length > 1) {
                // Skip the first option (original graphics) and add the rest
                for (int i = 1; i < originalOptions.length; i++) {
                    newOptions.add(originalOptions[i]);
                }
            }

            return new OptionBlock("", newOptions.toArray(new Option<?>[0]));

        } catch (Exception e) {
            // Return original block if enhancement fails
            return originalBlock;
        }
    }

    /**
     * Extract options from an OptionBlock using reflection
     */
    private static Option<?>[] getOptionsFromBlock(OptionBlock block) {
        try {
            var field = block.getClass().getDeclaredField("options");
            field.setAccessible(true);
            return (Option<?>[]) field.get(block);
        } catch (Exception e) {
            return null;
        }
    }
}