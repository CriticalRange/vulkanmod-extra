package com.criticalrange.mixin.steady_debug_hud;

import com.criticalrange.VulkanModExtra;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Steady debug HUD optimization mixin
 * Stabilizes debug screen for consistent performance monitoring
 * Implementation based on proven Sodium Extra pattern
 */
@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugScreenOverlay {
    @Shadow protected abstract void renderLines(GuiGraphics guiGraphics, List<String> list, boolean bl);

    @Unique
    private final List<String> leftTextCache = new ArrayList<>();
    @Unique
    private final List<String> rightTextCache = new ArrayList<>();
    @Unique
    private long nextTime = 0L;
    @Unique
    private boolean rebuild = true;

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void vulkanmodExtra$preRender(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG.performanceSettings.steadyDebugHud) {
            final long currentTime = Util.getMillis();
            if (currentTime > this.nextTime) {
                this.rebuild = true;
                this.nextTime = currentTime + (VulkanModExtra.CONFIG.performanceSettings.steadyDebugHudRefreshInterval * 50L);
            } else {
                this.rebuild = false;
            }
        } else {
            this.rebuild = true;
        }
    }

    @Redirect(method = "drawGameInformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;renderLines(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;Z)V"))
    public void vulkanmodExtra$redirectDrawLeftText(DebugScreenOverlay instance, GuiGraphics guiGraphics, List<String> text, boolean left) {
        if (this.rebuild) {
            this.leftTextCache.clear();
            this.leftTextCache.addAll(text);
        }
        this.renderLines(guiGraphics, this.leftTextCache, left);
    }

    @Redirect(method = "drawSystemInformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;renderLines(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;Z)V"))
    public void vulkanmodExtra$redirectDrawRightText(DebugScreenOverlay instance, GuiGraphics guiGraphics, List<String> text, boolean left) {
        if (this.rebuild) {
            this.rightTextCache.clear();
            this.rightTextCache.addAll(text);
        }
        this.renderLines(guiGraphics, this.rightTextCache, left);
    }
}
