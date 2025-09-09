package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls painting rendering based on configuration
 * Simple pattern compatible with Minecraft 1.21.1
 */
@Mixin(PaintingEntityRenderer.class)
public class MixinPaintingRenderer {

    @Inject(at = @At("HEAD"), method = "render*", cancellable = true)
    public void vulkanmodExtra$onRender(CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.renderSettings.painting) {
            ci.cancel();
        }
    }
}