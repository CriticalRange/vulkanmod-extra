package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Armor stand rendering control mixin for Minecraft 1.21.2+
 * Uses updated method signatures for 1.21.2+ architecture
 */
@Mixin(ArmorStandEntityRenderer.class)
public class MixinArmorStandEntityRenderer {

    @Inject(method = "hasLabel(Lnet/minecraft/entity/decoration/ArmorStandEntity;D)Z", at = @At("HEAD"), cancellable = true)
    protected boolean vulkanmodExtra$controlArmorStandLabel(ArmorStandEntity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (VulkanModExtra.CONFIG == null || VulkanModExtra.CONFIG.renderSettings == null) {
            return false;
        }

        var renderSettings = VulkanModExtra.CONFIG.renderSettings;

        // Master toggle - if disabled, show no labels
        if (!renderSettings.armorStandNameTag) {
            cir.setReturnValue(false);
            return false;
        }

        return false;
    }
}