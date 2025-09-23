package com.criticalrange.mixins.extra;

import com.criticalrange.client.VulkanModExtraClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to hook into Minecraft client shutdown for proper cleanup
 */
@Mixin(MinecraftClient.class)
public class MixinMinecraftClientShutdown {

    /**
     * Hook into client shutdown to cleanup VulkanMod Extra resources
     */
    @Inject(
        method = "close",
        at = @At("HEAD")
    )
    private void vulkanmodExtra$onClientShutdown(CallbackInfo ci) {
        // Cleanup VulkanMod Extra resources before Minecraft shuts down
        VulkanModExtraClient.onClientShutdown();
    }

    /**
     * Hook into stop method as additional cleanup point
     */
    @Inject(
        method = "stop",
        at = @At("HEAD")
    )
    private void vulkanmodExtra$onClientStop(CallbackInfo ci) {
        // Additional cleanup during client stop
        VulkanModExtraClient.onClientShutdown();
    }
}