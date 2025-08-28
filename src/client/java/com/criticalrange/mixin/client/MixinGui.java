package com.criticalrange.mixin.client;

import com.criticalrange.client.VulkanModExtraClientMod;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to hook into Minecraft's GUI rendering system
 * This allows us to render VulkanMod Extra HUD elements
 */
@Mixin(Gui.class)
public class MixinGui {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, Object class_9779, float partialTicks, CallbackInfo ci) {
        VulkanModExtraClientMod.onHudRender(guiGraphics, partialTicks);
    }
}
