package com.criticalrange.mixin.entity_rendering;

import com.criticalrange.client.config.VulkanModExtraClientConfig;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls armor stand rendering based on configuration
 */
@Mixin(ArmorStandRenderer.class)
public class MixinArmorStandRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo ci) {
        if (!VulkanModExtraClientConfig.getInstance().renderSettings.armorStand) {
            ci.cancel();
        }
    }
}