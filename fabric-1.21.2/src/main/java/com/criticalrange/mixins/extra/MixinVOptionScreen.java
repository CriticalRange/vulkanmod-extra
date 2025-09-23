package com.criticalrange.mixins.extra;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.integration.VulkanModPageFactory;
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

    // Shadow the optionPages field with correct type
    @Shadow
    private List<OptionPage> optionPages;

    // Inject into the addPages method after pages are added
    @Inject(method = "addPages", at = @At("TAIL"))
    private void onAddPages(CallbackInfo ci) {
        try {
            // Create and add VulkanMod Extra option pages
            List<Object> extraPages = VulkanModPageFactory.createOptionPages();
            if (extraPages != null && !extraPages.isEmpty()) {
                // Cast to OptionPage and add to the list
                for (Object page : extraPages) {
                    if (page instanceof OptionPage) {
                        this.optionPages.add((OptionPage) page);
                    }
                }
                LOGGER.info("Successfully added {} VulkanMod Extra pages to GUI", extraPages.size());
            } else {
                LOGGER.warn("No VulkanMod Extra pages were created");
            }
        } catch (Exception e) {
            LOGGER.error("GUI injection failed: {}", e.getMessage(), e);
        }
    }
}