package com.criticalrange.mixin.adaptive_sync;

import com.criticalrange.VulkanModExtra;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Adaptive sync optimization mixin
 * Optimizes VSync and adaptive sync for better performance
 * Implementation based on proven Sodium Extra pattern
 */
@Mixin(Window.class)
public class MixinWindow {

    @Redirect(method = "updateVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V", remap = false))
    private void vulkanmodExtra$setSwapInterval(int interval) {
        if (VulkanModExtra.CONFIG.extraSettings.useAdaptiveSync) {
            if (GLFW.glfwExtensionSupported("GLX_EXT_swap_control_tear") || GLFW.glfwExtensionSupported("WGL_EXT_swap_control_tear")) {
                GLFW.glfwSwapInterval(-1);
            } else {
                VulkanModExtra.LOGGER.warn("Adaptive vsync not supported, falling back to vanilla vsync state!");
                VulkanModExtra.CONFIG.extraSettings.useAdaptiveSync = false;
                VulkanModExtra.CONFIG.writeChanges();
                GLFW.glfwSwapInterval(interval);
            }
        } else {
            GLFW.glfwSwapInterval(interval);
        }
    }
}
