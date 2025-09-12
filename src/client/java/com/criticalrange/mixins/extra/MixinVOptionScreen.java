package com.criticalrange.mixins.extra;

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


    // Shadow the optionPages field with correct type
    @Shadow
    private List<OptionPage> optionPages;

    // Inject into the addPages method after pages are added
    @Inject(method = "addPages", at = @At("TAIL"))
    private void onAddPages(CallbackInfo ci) {

        try {
            // Create and add VulkanMod Extra option pages
            List<OptionPage> extraPages = VulkanModExtraIntegration.createVulkanModExtraOptionPages();
            if (extraPages != null && !extraPages.isEmpty()) {
                this.optionPages.addAll(extraPages);
            }

        } catch (Exception e) {
            LOGGER.error("GUI injection failed: {}", e.getMessage(), e);
        }
    }
}
