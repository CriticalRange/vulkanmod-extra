package com.criticalrange.mixins.extra;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FPS display optimization mixin
 * Provides performance monitoring and FPS display functionality
 * Integrates with the FPS display feature for enhanced performance tracking
 */
@Mixin(GameRenderer.class)
public class MixinFPSGameRenderer {

    // Removed empty injection points to reduce render loop overhead
    // FPS tracking is handled directly by the HUD system
}