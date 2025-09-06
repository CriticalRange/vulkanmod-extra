package com.criticalrange.mixin.instant_sneak;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
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
    private float eyeHeight;

    @Shadow
    private Entity entity;

    @Inject(at = @At("HEAD"), method = "tick")
    public void vulkanmodExtra$noLerp(CallbackInfo ci) {
        if (VulkanModExtra.CONFIG.extraSettings.instantSneak && this.entity != null) {
            this.eyeHeight = this.entity.getEyeHeight();
        }
    }
}
