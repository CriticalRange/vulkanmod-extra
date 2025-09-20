package com.criticalrange.mixins.extra;

import com.criticalrange.client.VulkanModExtraClient;
import com.criticalrange.util.VersionHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to hook into Minecraft's GUI rendering system
 * This allows us to render VulkanMod Extra HUD elements
 * Uses version-conditional rendering for compatibility
 */
@Mixin(InGameHud.class)
public class MixinGui {

    @Inject(method = "render", at = @At("TAIL"))
    private void vulkanmodExtra$onRender(DrawContext drawContext, Object tickCounter, CallbackInfo ci) {
        float tickDelta = getTickDelta(tickCounter);
        VulkanModExtraClient.onHudRender(drawContext, tickDelta);
    }

    /**
     * Version-conditional tick delta extraction
     * Handles different parameter types between Minecraft versions
     */
    private float getTickDelta(Object tickCounter) {
        if (VersionHelper.IS_1_21_5) {
            // 1.21.5: RenderTickCounter with getTickDelta(boolean)
            try {
                var method = tickCounter.getClass().getMethod("getTickDelta", boolean.class);
                return (Float) method.invoke(tickCounter, true);
            } catch (Exception e) {
                return 1.0f; // Fallback
            }
        } else {
            // 1.21.1-1.21.4: Simple float parameter or different structure
            try {
                // Try to extract tick delta via reflection
                if (tickCounter instanceof Float) {
                    return (Float) tickCounter;
                } else {
                    // Try common method names
                    var method = tickCounter.getClass().getMethod("getTickDelta");
                    return (Float) method.invoke(tickCounter);
                }
            } catch (Exception e) {
                return 1.0f; // Fallback value
            }
        }
    }
}
