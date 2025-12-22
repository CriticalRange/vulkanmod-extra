package com.criticalrange.mixins.vulkanmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.WorldRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents OpenGL transparency shader loading in FABULOUS mode when VulkanMod is active
 * Provides fallback framebuffers to prevent null pointer exceptions
 */
@Mixin(WorldRenderer.class)
public class MixinVulkanModTransparencyShader {

    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod-Extra/FABULOUS");

    /**
     * Block the loading of OpenGL transparency post-processor that conflicts with VulkanMod
     * This prevents the crash while allowing other FABULOUS features to work
     */
    @Inject(method = "loadTransparencyPostProcessor", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$blockTransparencyShader(CallbackInfo ci) {
        LOGGER.info("FABULOUS mode compatibility: Blocked OpenGL transparency shader");
        // Cancel OpenGL transparency shader loading when VulkanMod is active
        ci.cancel();
    }

    /**
     * Block the transparency processor reset to prevent any lingering OpenGL calls
     */
    @Inject(method = "resetTransparencyPostProcessor", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$blockTransparencyReset(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "getTranslucentFramebuffer", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$getTranslucentFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        cir.setReturnValue(MinecraftClient.getInstance().getFramebuffer());
    }

    @Inject(method = "getParticlesFramebuffer", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$getParticlesFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        cir.setReturnValue(MinecraftClient.getInstance().getFramebuffer());
    }

    @Inject(method = "getWeatherFramebuffer", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$getWeatherFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        cir.setReturnValue(MinecraftClient.getInstance().getFramebuffer());
    }

    @Inject(method = "getCloudsFramebuffer", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$getCloudsFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        cir.setReturnValue(MinecraftClient.getInstance().getFramebuffer());
    }

    @Inject(method = "getEntityFramebuffer", at = @At("HEAD"), cancellable = true)
    private void vulkanmodExtra$getEntityFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        cir.setReturnValue(MinecraftClient.getInstance().getFramebuffer());
    }
}