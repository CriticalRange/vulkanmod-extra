package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls piston rendering based on configuration
 * 1.21.2: Still uses entity-based rendering - using wildcard to catch all render method variants
 */
@Mixin(PistonBlockEntityRenderer.class)
public class MixinPistonRenderer {

    /**
     * 1.21.2: Catch-all render method using wildcard pattern
     * This captures both specific PistonBlockEntity and generic BlockEntity render methods
     */
    @Inject(method = "render*", at = @At("HEAD"), cancellable = true, require = 0)
    private void onRender(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.piston) {
            ci.cancel();
        }
    }
}