package com.criticalrange.mixin.client.gui;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.client.VulkanModExtraIntegration;
import net.vulkanmod.config.option.OptionPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin to integrate VulkanMod Extra options into VulkanMod's GUI
 * Targets the addPages method which is called during initialization
 */
@Mixin(value = net.vulkanmod.config.gui.VOptionScreen.class, remap = false)
public abstract class MixinVOptionScreen {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra VOptionScreen Mixin");

    static {
        LOGGER.info("VOptionScreen mixin loaded successfully!");
    }

    // Shadow the optionPages field with correct type
    @Shadow
    private List<OptionPage> optionPages;

    // Inject into the addPages method after pages are added
    @Inject(method = "addPages", at = @At("TAIL"))
    private void onAddPages(CallbackInfo ci) {
        LOGGER.debug("VOptionScreen addPages completed, attempting injection...");

        try {
            // Create and add VulkanMod Extra option pages
            List<OptionPage> extraPages = VulkanModExtraIntegration.createVulkanModExtraOptionPages();
            if (extraPages != null && !extraPages.isEmpty()) {
                LOGGER.info("Adding {} VulkanMod Extra pages", extraPages.size());
                this.optionPages.addAll(extraPages);
                LOGGER.info("Successfully added VulkanMod Extra pages! Total pages: {}", this.optionPages.size());
            } else {
                LOGGER.debug("No extra pages to add");
            }

        } catch (Exception e) {
            LOGGER.error("GUI injection failed: {}", e.getMessage(), e);
        }
    }
}
