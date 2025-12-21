package com.criticalrange.mixins.render;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BeaconBlockEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Beacon beam control mixin for Minecraft 1.21.9+
 * 
 * MAJOR CHANGE in 1.21.9:
 * - BlockEntityRenderer now uses RenderState objects instead of direct entity access
 * - render() signature changed completely to use BeaconBlockEntityRenderState
 * - VertexConsumerProvider replaced with OrderedRenderCommandQueue
 * - Added CameraRenderState parameter
 * 
 * Uses require=0 to fail gracefully if signature doesn't match.
 */
@Mixin(BeaconBlockEntityRenderer.class)
public class MixinBeaconRenderer {

    /**
     * Control beacon beam rendering for 1.21.9+
     * Uses the new RenderState-based render method
     */
    @Inject(method = "render(Lnet/minecraft/client/render/block/entity/state/BeaconBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", 
            at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanmodExtra$checkBeaconRendering(BeaconBlockEntityRenderState state,
            MatrixStack matrixStack, OrderedRenderCommandQueue queue,
            CameraRenderState cameraState, CallbackInfo ci) {
        // Cancel beacon beam rendering if disabled
        if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.renderSettings != null &&
            !VulkanModExtra.CONFIG.renderSettings.beaconBeam) {
            ci.cancel();
        }
    }

    /**
     * Note: Beacon beam height modification via @ModifyArg may need adjustment
     * The renderBeam method also changed signature in 1.21.9 to use OrderedRenderCommandQueue
     * For now, the height modification is disabled in favor of basic on/off control.
     * 
     * TODO: Implement height modification for 1.21.9+ if needed:
     * New signature: renderBeam(MatrixStack, OrderedRenderCommandQueue, float, float, int, int, int)V
     */
}