package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * UNIVERSAL PATTERN: 1.21.2+ ArmorStand rendering control
 *
 * In 1.21.2+, ArmorStandEntityRenderer overrides its own render method
 * In 1.21.1, ArmorStands are handled by MixinArmorStandRenderer (LivingEntityRenderer)
 */
@Mixin(ArmorStandEntityRenderer.class)
public class MixinArmorStandEntityRenderer {


    /**
     * Universal: hasLabel method (works across all versions)
     */
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true, require = 0)
    private void controlNameTag(CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.renderSettings.armorStandNameTag) {
            cir.setReturnValue(false);
        }
    }
}