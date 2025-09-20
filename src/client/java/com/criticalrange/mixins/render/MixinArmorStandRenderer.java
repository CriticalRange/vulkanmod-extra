package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.util.VersionHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls armor stand rendering based on configuration
 * Uses MixinExtras for version-conditional method handling
 * Handles rendering differences between Minecraft versions
 */
@Mixin(LivingEntityRenderer.class)
public class MixinArmorStandRenderer {

    /**
     * Version-conditional armor stand rendering injection
     * Handles different render method signatures between versions
     */
    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", cancellable = true)
    public void vulkanmodExtra$onRenderArmorStand(LivingEntity entity, float f, float g,
                                                   MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
                                                   int i, CallbackInfo ci) {
        if (entity instanceof ArmorStandEntity) {
            // Fast config access - no ConfigurationManager overhead
            if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
                if (!VulkanModExtra.CONFIG.renderSettings.armorStand) {
                    ci.cancel();
                }
            }
        }
    }

    /**
     * Wrap render operations to handle version-specific rendering differences
     * This ensures compatibility across all Minecraft versions
     */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "render", remap = false))
    private void vulkanmodExtra$wrapRender(LivingEntityRenderer renderer, LivingEntity entity,
                                           float entityYaw, float partialTicks, MatrixStack poseStack,
                                           VertexConsumerProvider buffer, int packedLight,
                                           Operation<Void> original) {
        // Check if this is an armor stand and if rendering is disabled
        if (entity instanceof ArmorStandEntity) {
            if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
                if (!VulkanModExtra.CONFIG.renderSettings.armorStand) {
                    return; // Don't render armor stand if disabled
                }
            }
        }

        // Handle version-specific rendering optimizations
        if (VersionHelper.IS_POST_1_21_1) {
            // 1.21.2+ version: Enhanced armor stand rendering
            renderArmorStandEnhanced(renderer, entity, entityYaw, partialTicks, poseStack, buffer, packedLight, original);
        } else {
            // 1.21.1 version: Basic armor stand rendering
            renderArmorStandBasic(renderer, entity, entityYaw, partialTicks, poseStack, buffer, packedLight, original);
        }
    }

    /**
     * Enhanced armor stand rendering for 1.21.2+
     */
    private void renderArmorStandEnhanced(LivingEntityRenderer renderer, LivingEntity entity,
                                          float entityYaw, float partialTicks, MatrixStack poseStack,
                                          VertexConsumerProvider buffer, int packedLight,
                                          Operation<Void> original) {
        try {
            // Apply enhanced rendering optimizations for newer versions
            if (entity instanceof ArmorStandEntity armorStand) {
                // Check for visibility optimizations
                if (isArmorStandVisible(armorStand)) {
                    original.call(renderer, entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
                }
            } else {
                // Not an armor stand, render normally
                original.call(renderer, entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to render armor stand with enhanced method", e);
            // Fallback to original rendering
            original.call(renderer, entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }

    /**
     * Basic armor stand rendering for 1.21.1
     */
    private void renderArmorStandBasic(LivingEntityRenderer renderer, LivingEntity entity,
                                       float entityYaw, float partialTicks, MatrixStack poseStack,
                                       VertexConsumerProvider buffer, int packedLight,
                                       Operation<Void> original) {
        try {
            // Apply basic rendering for older versions
            original.call(renderer, entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to render armor stand with basic method", e);
            // Can't fallback further, just log the error
        }
    }

    /**
     * Check if armor stand should be visible (optimization)
     */
    private boolean isArmorStandVisible(ArmorStandEntity armorStand) {
        // Basic visibility checks
        if (armorStand == null) return false;
        if (!armorStand.isAlive()) return false;
        // Basic visibility optimizations (simplified for now)
        if (armorStand.isInvisible()) return false;

        // Distance-based culling (basic implementation)
        var minecraft = net.minecraft.client.MinecraftClient.getInstance();
        if (minecraft.player != null) {
            double distance = minecraft.player.squaredDistanceTo(armorStand);
            // Simple distance check (64 blocks)
            if (distance > 64 * 64) {
                return false;
            }
        }

        return true;
    }
}