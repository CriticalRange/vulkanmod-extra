package com.criticalrange.mixins.extra;

import com.criticalrange.client.VulkanModExtraClientMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to hook into Minecraft's GUI rendering system
 * This allows us to render VulkanMod Extra HUD elements
 */
@Mixin(InGameHud.class)
public class MixinGui {
    @Inject(method = "render", at = @At("TAIL"))
    private void vulkanmodExtra$onRender(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Use a reasonable tickDelta value
        float tickDelta = 1.0f; // Fallback value that works for HUD rendering
        VulkanModExtraClientMod.onHudRender(drawContext, tickDelta);
    }
}