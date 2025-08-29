package com.criticalrange.mixin.client;

import com.criticalrange.client.VulkanModExtraClientMod;
import net.minecraft.client.DeltaTracker;
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
    private void vulkanmodExtra$onRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        VulkanModExtraClientMod.onHudRender(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    }
}
