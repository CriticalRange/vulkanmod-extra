package com.criticalrange.mixin.entity_rendering;

import com.criticalrange.VulkanModExtra;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls armor stand rendering based on configuration
 * Simple pattern compatible with Minecraft 1.21.1
 */
@Mixin(ArmorStandRenderer.class)
public class MixinArmorStandRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$onRender(CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.renderSettings.armorStand) {
            ci.cancel();
        }
    }
}