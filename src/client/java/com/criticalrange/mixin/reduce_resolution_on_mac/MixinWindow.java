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
                // Reduce effective resolution on macOS for better performance with Retina displays
                // This helps with high-DPI performance issues by using a lower internal resolution
                double reductionFactor = 0.75; // 75% of native resolution
                
                // Calculate reduced dimensions
                int reducedWidth = (int) (this.width * reductionFactor);
                int reducedHeight = (int) (this.height * reductionFactor);
                
                // Apply the reduction by modifying internal dimensions
                // This maintains display scaling while reducing rendering cost
                if (reducedWidth > 640 && reducedHeight > 480) { // Maintain minimum usable size
                    try {
                        // Use reflection to set reduced dimensions
                        this.width = reducedWidth;
                        this.height = reducedHeight;
                        VulkanModExtra.LOGGER.info("Applied macOS resolution reduction: {}x{} -> {}x{}", 
                                                   (int)(reducedWidth / reductionFactor), 
                                                   (int)(reducedHeight / reductionFactor),
                                                   reducedWidth, reducedHeight);
                    } catch (Exception e) {
                        VulkanModExtra.LOGGER.warn("Failed to apply macOS resolution reduction: {}", e.getMessage());
                    }
                }
            }
        }
    }
}
