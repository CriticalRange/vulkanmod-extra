package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * Beacon beam control mixin with height adjustment
 * Controls both beacon beam toggle and height based on configuration
 * Uses @ModifyConstant to target hardcoded height values
 */
@Mixin(BeaconBlockEntityRenderer.class)
public class MixinBeaconRenderer {

    /**
     * 1.21.2: Catch-all render method using wildcard pattern
     * Controls beacon beam rendering toggle
     */
    @Inject(method = "render*", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$controlBeaconRendering(CallbackInfo ci) {
        // Cancel beacon beam rendering if disabled
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null &&
            !VulkanModExtra.CONFIG.renderSettings.beaconBeam) {
            ci.cancel();
        }
    }

    /**
     * Modify the hardcoded beacon beam height constants
     * This targets the 1024 value used for infinite beacon beam height
     */
    @ModifyConstant(
        method = "render*",
        constant = @Constant(intValue = 1024)
    )
    private int vulkanmodExtra$modifyBeaconHeightConstant(int originalValue) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            // Replace hardcoded 1024 with configured height
            int configuredHeight = VulkanModExtra.CONFIG.renderSettings.beaconBeamHeight;
            // Clamp to reasonable range (32-512)
            return Math.max(32, Math.min(512, configuredHeight));
        }
        return originalValue;
    }

    /**
     * Alternative approach: modify float constants for beacon height
     */
    @ModifyConstant(
        method = "render*",
        constant = @Constant(floatValue = 1024.0f)
    )
    private float vulkanmodExtra$modifyBeaconHeightFloat(float originalValue) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            // Replace hardcoded 1024.0f with configured height
            float configuredHeight = (float) VulkanModExtra.CONFIG.renderSettings.beaconBeamHeight;
            // Clamp to reasonable range (32-512)
            return Math.max(32.0f, Math.min(512.0f, configuredHeight));
        }
        return originalValue;
    }

    /**
     * Target MAX_BEAM_HEIGHT constant if it exists
     */
    @ModifyConstant(
        method = "*",
        constant = @Constant(intValue = 1024, ordinal = 0)
    )
    private int vulkanmodExtra$modifyMaxBeamHeight(int originalValue) {
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null) {
            // Replace MAX_BEAM_HEIGHT with configured height
            int configuredHeight = VulkanModExtra.CONFIG.renderSettings.beaconBeamHeight;
            return Math.max(32, Math.min(512, configuredHeight));
        }
        return originalValue;
    }
}