package com.criticalrange.mixins.extra;

import com.criticalrange.render.TextRenderHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HUD rendering mixin for Minecraft 1.21.9
 * Uses version-specific text rendering to handle API differences
 */
@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render", at = @At("HEAD"))
    private void vulkanmodExtra$onHudRender(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Render FPS display using version-specific helper
        TextRenderHelper.renderFpsDisplay(drawContext);
    }
}

