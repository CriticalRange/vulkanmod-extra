package com.criticalrange.mixins.extra;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.util.MonitorInfoUtil;
import net.minecraft.util.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
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
@Mixin(DebugHud.class)
public abstract class MixinDebugScreenOverlay {
    @Shadow private void drawText(DrawContext drawContext, List<String> list, boolean bl) {}

    @Unique
    private final List<String> leftTextCache = new ArrayList<>();
    @Unique
    private final List<String> rightTextCache = new ArrayList<>();
    @Unique
    private long nextTime = 0L;
    @Unique
    private boolean rebuild = true;

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void vulkanmodExtra$preRender(DrawContext guiGraphics, CallbackInfo ci) {
        if (VulkanModExtra.CONFIG.extraSettings.steadyDebugHud) {
            final long currentTime = Util.getEpochTimeMs();
            if (currentTime > this.nextTime) {
                this.rebuild = true;
                this.nextTime = currentTime + (VulkanModExtra.CONFIG.extraSettings.steadyDebugHudRefreshInterval * 50L);
            } else {
                this.rebuild = false;
            }
        } else {
            this.rebuild = true;
        }
    }

    @Redirect(method = "drawLeftText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;drawText(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;Z)V"))
    public void vulkanmodExtra$redirectDrawLeftText(DebugHud instance, DrawContext guiGraphics, List<String> text, boolean left) {
        if (this.rebuild) {
            this.leftTextCache.clear();
            this.leftTextCache.addAll(text);
        }
        this.drawText(guiGraphics, this.leftTextCache, left);
    }

    @Redirect(method = "drawRightText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;drawText(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;Z)V"))
    public void vulkanmodExtra$redirectDrawRightText(DebugHud instance, DrawContext guiGraphics, List<String> text, boolean left) {
        if (this.rebuild) {
            this.rightTextCache.clear();
            this.rightTextCache.addAll(text);
        }
        this.drawText(guiGraphics, this.rightTextCache, left);
    }
  }
