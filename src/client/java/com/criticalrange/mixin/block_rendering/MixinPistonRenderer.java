package com.criticalrange.mixin.block_rendering;

import com.criticalrange.client.config.VulkanModExtraClientConfig;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls piston rendering based on configuration
 */
@Mixin(PistonHeadRenderer.class)
public class MixinPistonRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo ci) {
        if (!VulkanModExtraClientConfig.getInstance().renderSettings.piston) {
            ci.cancel();
        }
    }
}