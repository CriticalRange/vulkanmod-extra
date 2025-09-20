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
 * GUI mixin for VulkanMod Extra HUD rendering
 * Simplified to avoid MixinExtras parameter order complexities
 * Demonstrates clean, working mixin patterns
 */
@Mixin(InGameHud.class)
public class MixinGui {

    /**
     * Render our HUD elements at the end of GUI rendering
     * Uses traditional parameter access for maximum compatibility
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void vulkanmodExtra$onRender(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo ci) {
        float tickDelta = tickCounter.getTickDelta(true);
        VulkanModExtraClient.onHudRender(drawContext, tickDelta);
    }
}
