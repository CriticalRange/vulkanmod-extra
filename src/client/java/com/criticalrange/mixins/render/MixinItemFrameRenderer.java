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
 * Simplified to use only @Inject methods for compatibility
 * Works across all Minecraft versions without version-specific signatures
 */
@Mixin(ItemFrameEntityRenderer.class)
public class MixinItemFrameRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    public void vulkanmodExtra$onRender(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            if (!VulkanModExtra.CONFIG.renderSettings.itemFrame) {
                ci.cancel();
            }
        }
    }

    /**
     * Disable item frame name tags based on configuration
     * Works with both hasLabel method signatures across versions
     */
    @Inject(method = "hasLabel*", at = @At(value = "HEAD"), cancellable = true)
    private void vulkanmodExtra$shouldShowName(CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            if (!VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag) {
                cir.setReturnValue(false);
            }
        }
    }
}