package com.criticalrange.mixin.entity_rendering;

import com.criticalrange.client.config.VulkanModExtraClientConfig;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls item frame rendering based on configuration
 */
@Mixin(ItemFrameRenderer.class)
public class MixinItemFrameRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo ci) {
        if (!VulkanModExtraClientConfig.getInstance().renderSettings.itemFrame) {
            ci.cancel();
        }
    }
}