package com.criticalrange.mixin.entity_rendering;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Controls item frame rendering based on configuration
 * Simple pattern compatible with Minecraft 1.21.1
 */
@Mixin(ItemFrameRenderer.class)
public class MixinItemFrameRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    public void vulkanmodExtra$onRender(CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.renderSettings.itemFrame) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldShowName*", at = @At(value = "HEAD"), cancellable = true)
    private <T extends ItemFrame> void vulkanmodExtra$shouldShowName(T itemFrame, double d, CallbackInfoReturnable<Boolean> cir) {
        if (!VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag) {
            cir.setReturnValue(false);
        }
    }
}