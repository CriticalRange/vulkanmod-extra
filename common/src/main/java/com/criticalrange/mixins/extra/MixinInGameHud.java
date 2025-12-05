package com.criticalrange.mixins.extra;

import com.criticalrange.client.VulkanModExtraClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HUD rendering mixin - hooks into Minecraft's HUD rendering system
 * This enables the FPS counter and other overlay features to be displayed
 */
@Mixin(InGameHud.class)
public class MixinInGameHud {

    // 1.21.1 signature: render(DrawContext, float, CallbackInfo)
    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;F)V", at = @At("HEAD"))
    private void vulkanmodExtra$onHudRenderLegacy(DrawContext drawContext, float tickDelta, CallbackInfo ci) {
        VulkanModExtraClient.onHudRender(drawContext, tickDelta);
    }

    // 1.21.2+ signature: render(DrawContext, RenderTickCounter, CallbackInfo)
    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("HEAD"))
    private void vulkanmodExtra$onHudRender(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo ci) {
        VulkanModExtraClient.onHudRender(drawContext, tickCounter.getLastFrameDuration());
    }
}
