package com.criticalrange.mixin.sky;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls individual sky element rendering (sun, moon, stars)
 */
@Mixin(LevelRenderer.class)
public class MixinSkyElements {

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;begin(Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)V", ordinal = 1), cancellable = true)
    private void vulkanmodExtra$checkSunRendering(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.detailSettings.sun) {
            // Skip sun rendering by returning early
            ci.cancel();
        }
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;begin(Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)V", ordinal = 2), cancellable = true)
    private void vulkanmodExtra$checkMoonRendering(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.detailSettings.moon) {
            // Skip moon rendering by returning early
            ci.cancel();
        }
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;begin(Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)V", ordinal = 3), cancellable = true)
    private void vulkanmodExtra$checkStarsRendering(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG != null && !VulkanModExtra.CONFIG.detailSettings.stars) {
            // Skip stars rendering by returning early
            ci.cancel();
        }
    }
}