package com.criticalrange.mixin.client.gui;

import com.criticalrange.client.VulkanModExtraIntegration;
import net.vulkanmod.config.option.OptionPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Mixin to integrate VulkanMod Extra options into VulkanMod's GUI
 * This mixin injects into VulkanMod's VOptionScreen to add extra option pages
 */
@Mixin(targets = "net.vulkanmod.config.gui.VOptionScreen")
public class MixinVOptionScreen {
    static {
        System.out.println("[VulkanMod Extra] MixinVOptionScreen class loaded!");
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addVulkanModExtraPages(CallbackInfo ci) {
        System.out.println("[VulkanMod Extra] Mixin applied successfully! Injecting pages at init TAIL...");
        try {
            // Use the integration system to inject pages
            VulkanModExtraIntegration.injectPagesIntoVulkanMod(this);
            System.out.println("[VulkanMod Extra] Page injection completed!");
        } catch (Exception e) {
            System.err.println("[VulkanMod Extra] Failed to add VulkanMod Extra pages: " + e.getMessage());
            e.printStackTrace();
        }
    }




}
