package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls piston rendering based on configuration
 */
@Mixin(PistonBlockEntityRenderer.class)
public class MixinPistonRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.piston) {
            ci.cancel();
        }
    }
}