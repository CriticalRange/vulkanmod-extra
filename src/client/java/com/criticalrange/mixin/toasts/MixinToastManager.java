package com.criticalrange.mixin.toasts;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Toast notification control mixin
 * Controls toast display based on user preferences
 */
@Mixin(ToastManager.class)
public class MixinToastManager {

    @Inject(method = "add", at = @At("HEAD"))
    private void vulkanmodExtra$controlToastDisplay(Toast toast, CallbackInfo ci) {
        // Check if toasts are globally disabled
        if (!VulkanModExtra.CONFIG.extraSettings.toasts) {
            VulkanModExtra.LOGGER.debug("Toast display disabled globally");
            return;
        }

        // Check specific toast types
        String toastClassName = toast.getClass().getSimpleName();

        // Advancement toasts
        if (toastClassName.contains("Advancement") && !VulkanModExtra.CONFIG.extraSettings.advancementToast) {
            VulkanModExtra.LOGGER.debug("Advancement toast blocked: {}", toastClassName);
            return;
        }

        // Recipe toasts
        if (toastClassName.contains("Recipe") && !VulkanModExtra.CONFIG.extraSettings.recipeToast) {
            VulkanModExtra.LOGGER.debug("Recipe toast blocked: {}", toastClassName);
            return;
        }

        // System toasts
        if (toastClassName.contains("System") && !VulkanModExtra.CONFIG.extraSettings.systemToast) {
            VulkanModExtra.LOGGER.debug("System toast blocked: {}", toastClassName);
            return;
        }

        // Tutorial toasts
        if (toast instanceof TutorialToast && !VulkanModExtra.CONFIG.extraSettings.tutorialToast) {
            VulkanModExtra.LOGGER.debug("Tutorial toast blocked");
            return;
        }

        VulkanModExtra.LOGGER.debug("Toast allowed: {}", toastClassName);
    }
}
