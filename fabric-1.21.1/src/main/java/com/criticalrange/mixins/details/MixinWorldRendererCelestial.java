package com.criticalrange.mixins.details;

import com.criticalrange.VulkanModExtra;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Controls individual celestial body rendering (sun, moon, stars) for Minecraft 1.21.1
 * Uses MixinExtras for better mod compatibility
 * Prevents BufferBuilder warnings by skipping geometry creation when celestial bodies are disabled
 */
@Mixin(WorldRenderer.class)
public class MixinWorldRendererCelestial {

    @Shadow
    private VertexBuffer starsBuffer;

    @Shadow
    private VertexBuffer lightSkyBuffer;

    // Track whether we're currently in sun or moon rendering context
    private boolean isInSunContext = false;
    private boolean isInMoonContext = false;

    // Dummy VertexConsumer for disabled celestial bodies
    private static net.minecraft.client.render.VertexConsumer dummyVertexConsumer = null;

    /**
     * Track when we're in sun rendering context by detecting sun texture setup
     */
    @WrapOperation(
        method = "renderSky",
        at = @At(value = "INVOKE",
                 target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V")
    )
    private void vulkanmodExtra$trackCelestialContext(int unit, net.minecraft.util.Identifier texture, Operation<Void> original) {
        // Update context tracking based on texture
        if (texture != null) {
            String texturePath = texture.toString();
            isInSunContext = texturePath.contains("sun");
            isInMoonContext = texturePath.contains("moon");
        } else {
            isInSunContext = false;
            isInMoonContext = false;
        }

        // Proceed with texture setting
        original.call(unit, texture);
    }

    /**
     * Control vertex building by wrapping BufferBuilder.vertex calls
     * Return dummy VertexConsumer when celestial bodies are disabled
     */
    @WrapOperation(
        method = "renderSky",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/render/BufferBuilder;vertex(Lorg/joml/Matrix4f;FFF)Lnet/minecraft/client/render/VertexConsumer;")
    )
    private net.minecraft.client.render.VertexConsumer vulkanmodExtra$wrapVertexCalls(
        BufferBuilder instance,
        org.joml.Matrix4f matrix,
        float x, float y, float z,
        Operation<net.minecraft.client.render.VertexConsumer> original
    ) {
        // Return dummy VertexConsumer for disabled sun
        if (isInSunContext &&
            VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.detailSettings != null &&
            (!VulkanModExtra.CONFIG.detailSettings.sky || !VulkanModExtra.CONFIG.detailSettings.sun)) {
            return getDummyVertexConsumer();
        }

        // Return dummy VertexConsumer for disabled moon
        if (isInMoonContext &&
            VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.detailSettings != null &&
            (!VulkanModExtra.CONFIG.detailSettings.sky || !VulkanModExtra.CONFIG.detailSettings.moon)) {
            return getDummyVertexConsumer();
        }

        // Otherwise, proceed normally
        return original.call(instance, matrix, x, y, z);
    }

    /**
     * Control sky rendering by wrapping VertexBuffer.draw() calls
     * Skip draw calls for disabled sky elements (stars, sky gradient)
     */
    @WrapOperation(
        method = "renderSky",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V")
    )
    private void vulkanmodExtra$wrapSkyBufferDraw(
        VertexBuffer instance,
        org.joml.Matrix4f modelMatrix,
        org.joml.Matrix4f projectionMatrix,
        net.minecraft.client.gl.ShaderProgram shaderProgram,
        Operation<Void> original
    ) {
        // Skip stars drawing when disabled
        if (instance == this.starsBuffer &&
            VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.detailSettings != null &&
            (!VulkanModExtra.CONFIG.detailSettings.sky || !VulkanModExtra.CONFIG.detailSettings.stars)) {
            return; // Skip the draw call
        }

        // Skip sky gradient drawing when disabled
        if (instance == this.lightSkyBuffer &&
            VulkanModExtra.CONFIG != null &&
            VulkanModExtra.CONFIG.detailSettings != null &&
            (!VulkanModExtra.CONFIG.detailSettings.sky || !VulkanModExtra.CONFIG.detailSettings.skyGradient)) {
            return; // Skip the draw call
        }

        // Otherwise, proceed with normal drawing
        original.call(instance, modelMatrix, projectionMatrix, shaderProgram);
    }

    /**
     * Get or create a dummy VertexConsumer that accepts method calls but doesn't build anything
     */
    private static net.minecraft.client.render.VertexConsumer getDummyVertexConsumer() {
        if (dummyVertexConsumer == null) {
            dummyVertexConsumer = new DummyVertexConsumer();
        }
        return dummyVertexConsumer;
    }

    /**
     * Dummy VertexConsumer that accepts all method calls but does nothing
     */
    private static class DummyVertexConsumer implements net.minecraft.client.render.VertexConsumer {
        public net.minecraft.client.render.VertexConsumer vertex(double x, double y, double z) {
            return this;
        }

        public net.minecraft.client.render.VertexConsumer vertex(float x, float y, float z) {
            return this;
        }

        public net.minecraft.client.render.VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        public net.minecraft.client.render.VertexConsumer texture(float u, float v) {
            return this;
        }

        public net.minecraft.client.render.VertexConsumer overlay(int u, int v) {
            return this;
        }

        public net.minecraft.client.render.VertexConsumer light(int u, int v) {
            return this;
        }

        public net.minecraft.client.render.VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        public void next() {
            // Do nothing
        }
    }

    /**
     * Control BufferBuilder.end() calls to handle empty buffers from dummy VertexConsumer
     * Return null when buffer is empty to skip draw calls
     */
    @WrapOperation(
        method = "renderSky",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/render/BufferBuilder;end()Lnet/minecraft/client/render/BuiltBuffer;")
    )
    private net.minecraft.client.render.BuiltBuffer vulkanmodExtra$wrapBufferEnd(
        BufferBuilder instance,
        Operation<net.minecraft.client.render.BuiltBuffer> original
    ) {
        try {
            return original.call(instance);
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("BufferBuilder was empty")) {
                // Return null for empty buffers (from disabled celestial bodies)
                return null;
            }
            throw e; // Re-throw other IllegalStateExceptions
        }
    }

    /**
     * Control BufferRenderer draw calls to handle null BuiltBuffer cases
     * Skip drawing when BuiltBuffer is null (from empty BufferBuilder)
     */
    @WrapOperation(
        method = "renderSky",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/render/BufferRenderer;drawWithGlobalProgram(Lnet/minecraft/client/render/BuiltBuffer;)V")
    )
    private void vulkanmodExtra$wrapDrawWithGlobalProgram(
        net.minecraft.client.render.BuiltBuffer buffer,
        Operation<Void> original
    ) {
        // Only draw if buffer is not null (prevents crashes from empty BufferBuilder)
        if (buffer != null) {
            original.call(buffer);
        }
    }

}