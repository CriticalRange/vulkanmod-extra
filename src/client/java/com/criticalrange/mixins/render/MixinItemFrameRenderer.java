package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Controls item frame rendering with multi-version support
 * Handles both 1.21.1 entity-based and 1.21.2+ render state-based rendering
 */
@Mixin(ItemFrameEntityRenderer.class)
public class MixinItemFrameRenderer {

    /**
     * Universal item frame rendering injection - targets any render method
     * Works across all Minecraft versions by targeting method name only
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlItemFrameRendering(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.itemFrame) {
            ci.cancel();
        }
    }

    /**
     * Controls item frame name tag display for both versions
     * Works with entity-based hasLabel method
     */
    @Inject(method = "hasLabel(Lnet/minecraft/entity/decoration/ItemFrameEntity;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlItemFrameNameTag(ItemFrameEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag) {
            cir.setReturnValue(false);
        }
    }
}