package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.util.VersionHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Controls item frame rendering based on configuration
 * Uses MixinExtras for version-conditional method handling
 * Handles method signature differences between Minecraft versions
 */
@Mixin(ItemFrameEntityRenderer.class)
public class MixinItemFrameRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    public void vulkanmodExtra$onRender(CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.renderSettings.itemFrame) {
            ci.cancel();
        }
    }

    /**
     * Version-conditional hasLabel injection
     * 1.21.1: hasLabel(T, double)
     * 1.21.2+: hasLabel(T) - no double parameter
     */
    @Inject(method = "hasLabel", at = @At(value = "HEAD"), cancellable = true)
    private void vulkanmodExtra$shouldShowName(ItemFrameEntity itemFrame, CallbackInfoReturnable<Boolean> cir) {
        if (!VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Wrap hasLabel method to handle version-specific signatures
     * This ensures compatibility across all Minecraft versions
     */
    @WrapOperation(method = "hasLabel", at = @At(value = "INVOKE", target = "hasLabel", remap = false))
    private boolean vulkanmodExtra$wrapHasLabel(ItemFrameEntityRenderer renderer, ItemFrameEntity itemFrame, Operation<Boolean> original) {
        // Always check our config first
        if (!VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag) {
            return false;
        }

        // Handle version-specific method signatures
        if (VersionHelper.IS_1_21_1) {
            // 1.21.1 version: hasLabel(T itemFrame, double distance)
            try {
                // Call with a default distance for 1.21.1
                return callHasLabelWithDistance(renderer, itemFrame, 64.0);
            } catch (Exception e) {
                // Fallback to original call
                return original.call(renderer, itemFrame);
            }
        } else {
            // 1.21.2+ version: hasLabel(T itemFrame) - no distance parameter
            return original.call(renderer, itemFrame);
        }
    }

    /**
     * Helper method to call hasLabel with distance parameter for 1.21.1
     */
    private boolean callHasLabelWithDistance(ItemFrameEntityRenderer renderer, ItemFrameEntity itemFrame, double distance) {
        try {
            // Use reflection to call the method with distance parameter
            var method = renderer.getClass().getMethod("hasLabel", ItemFrameEntity.class, double.class);
            return (Boolean) method.invoke(renderer, itemFrame, distance);
        } catch (Exception e) {
            // Fallback: return true to show label
            return true;
        }
    }
}