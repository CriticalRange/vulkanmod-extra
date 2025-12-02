package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cloud rendering control mixin
 * Controls cloud rendering based on user preferences and performance settings
 * Basic version for compatibility across Minecraft versions
 */
@Mixin(WorldRenderer.class)
public class MixinSkyElements {

    /**
     * Basic cloud control using wildcard patterns
     * Platform-specific versions can override this with more precise targeting
     */
    @Inject(method = "render*", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlCloudRendering(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.detailSettings != null) {
            var settings = VulkanModExtra.CONFIG.detailSettings;

            // Disable clouds completely if distance is set to 0 or below
            if (settings.cloudDistance <= 0) {
                ci.cancel();
                return;
            }
        }
    }
}