package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Controls item frame rendering based on configuration
 * Simple pattern compatible with Minecraft 1.21.1
 */
@Mixin(ItemFrameEntityRenderer.class)
public class MixinItemFrameRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    public void vulkanmodExtra$onRender(CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.renderSettings.itemFrame) {
            ci.cancel();
        }
    }

    @Inject(method = "hasLabel", at = @At(value = "HEAD"), cancellable = true)
    private <T extends ItemFrameEntity> void vulkanmodExtra$shouldShowName(T itemFrame, double d, CallbackInfoReturnable<Boolean> cir) {
        if (!VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag) {
            cir.setReturnValue(false);
        }
    }
}