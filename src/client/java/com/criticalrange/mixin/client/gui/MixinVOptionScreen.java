package com.criticalrange.mixin.client.gui;

import com.criticalrange.client.VulkanModExtraIntegration;
import net.vulkanmod.config.option.OptionPage;
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

    static {
        System.out.println("[VulkanMod Extra] VOptionScreen mixin loaded successfully!");
    }

    // Shadow the optionPages field with correct type
    @Shadow
    private List<OptionPage> optionPages;

    // Inject into the addPages method after pages are added
    @Inject(method = "addPages", at = @At("TAIL"))
    private void onAddPages(CallbackInfo ci) {
        System.out.println("[VulkanMod Extra] VOptionScreen addPages completed, attempting injection...");

        try {
            // Create VulkanMod Extra pages and add them directly
            List<OptionPage> extraPages = VulkanModExtraIntegration.createVulkanModExtraOptionPages();
            if (extraPages != null && !extraPages.isEmpty()) {
                System.out.println("[VulkanMod Extra] Adding " + extraPages.size() + " VulkanMod Extra pages");
                this.optionPages.addAll(extraPages);
                System.out.println("[VulkanMod Extra] Successfully added VulkanMod Extra pages! Total pages: " + this.optionPages.size());
            } else {
                System.out.println("[VulkanMod Extra] No extra pages to add");
            }

        } catch (Exception e) {
            System.err.println("[VulkanMod Extra] GUI injection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }



}
