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

    /**
     * Hook into the main HUD rendering method
     * This is called every frame to render HUD elements
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void vulkanmodExtra$onHudRender(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Call our HUD renderer
        VulkanModExtraClient.onHudRender(drawContext, tickCounter.getTickProgress(false));
    }
}