package com.criticalrange.mixins.extra;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Instant sneak optimization mixin
 * Makes camera transitions instantaneous when sneaking for better responsiveness
 * Implementation based on proven Sodium Extra pattern
 */
@Mixin(Camera.class)
public class MixinCamera {

    @Shadow
    private float cameraY;

    @Shadow
    private Entity focusedEntity;

    @Inject(at = @At("HEAD"), method = "update")
    public void vulkanmodExtra$noLerp(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG.extraSettings.instantSneak && this.focusedEntity != null) {
            this.cameraY = this.focusedEntity.getEyeHeight(this.focusedEntity.getPose());
        }
    }
}
