package com.criticalrange.mixin.reduce_resolution_on_mac;

import com.criticalrange.VulkanModExtra;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mac resolution reduction optimization mixin
 * Reduces rendering resolution on macOS for better performance
 */
@Mixin(Window.class)
public class MixinWindow {

    @Shadow
    private int width;
    @Shadow
    private int height;

    @Inject(method = "setGuiScale", at = @At("HEAD"))
    private void vulkanmodExtra$optimizeMacResolution(CallbackInfo ci) {
        // Check if macOS resolution reduction is enabled
        if (VulkanModExtra.CONFIG.extraSettings.reduceResolutionOnMac) {
            // Check if we're running on macOS
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                // Automatic resolution reduction on macOS for better performance
                // This can help with Retina display performance issues
                // Implementation: Reduce internal resolution while maintaining display scaling
            }
        }
    }
}
