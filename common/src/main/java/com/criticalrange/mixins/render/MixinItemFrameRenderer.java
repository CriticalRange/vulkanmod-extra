package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * UNIVERSAL PATTERN: Multi-version entity rendering control
 *
 * Copy-paste template for any entity renderer:
 * 1. Replace "ItemFrame" with your entity type
 * 2. Replace "itemFrame" with your config setting
 * 3. Replace ItemFrameEntityRenderer with your specific renderer
 * 4. Replace ItemFrameEntity with your entity class
 */
@Mixin(ItemFrameEntityRenderer.class)
public class MixinItemFrameRenderer {

    /**
     * 1.21.1: Entity-based render method
     */
    @Inject(method = "render(Lnet/minecraft/entity/decoration/ItemFrameEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void controlRendering1_21_1(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.itemFrame) {
            ci.cancel();
        }
    }


    /**
     * Universal: hasLabel method (works across all versions)
     * For 1.21.1: hasLabel(ItemFrameEntity)
     * For 1.21.2+: hasLabel() with no parameters
     */
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true, require = 0)
    private void controlNameTag(CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.itemFrameNameTag) {
            cir.setReturnValue(false);
        }
    }
}