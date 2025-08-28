package com.criticalrange.client;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.client.config.VulkanModExtraClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * HUD overlay system for VulkanMod Extra features like FPS display and coordinates
 * Based on sodium-extra's HUD system
 */
public class VulkanModExtraHud {
    private final Minecraft minecraft;
    private int fps = 0;
    private long lastUpdateTime = 0;

    public VulkanModExtraHud() {
        this.minecraft = Minecraft.getInstance();
    }

    public void onHudRender(GuiGraphics guiGraphics, float partialTicks) {
        if (minecraft.player == null) return;

        // Update FPS counter
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= 1000) { // Update every second
            fps = minecraft.getFps();
            lastUpdateTime = currentTime;
        }

        renderOverlay(guiGraphics);
    }

    private void renderOverlay(GuiGraphics guiGraphics) {
        var config = VulkanModExtra.CONFIG.extraSettings;
        var clientConfig = VulkanModExtraClientConfig.getInstance();

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Calculate overlay position based on corner setting
        int x = getOverlayX(screenWidth, config.overlayCorner);
        int y = getOverlayY(screenHeight, config.overlayCorner);

        int lineHeight = 10;
        int currentY = y;

        // FPS display
        if (config.showFps) {
            String fpsText = "FPS: " + fps;
            if (config.showFPSExtended) {
                // Add extended FPS info
                fpsText += " (avg: " + fps + ")";
            }
            drawText(guiGraphics, fpsText, x, currentY, config.textContrast);
            currentY += lineHeight;
        }

        // Coordinates display
        if (config.showCoords) {
            Player player = minecraft.player;
            String coordsText = String.format("XYZ: %.1f / %.1f / %.1f",
                player.getX(), player.getY(), player.getZ());
            drawText(guiGraphics, coordsText, x, currentY, config.textContrast);
        }
    }

    private int getOverlayX(int screenWidth, com.criticalrange.config.VulkanModExtraConfig.OverlayCorner corner) {
        return switch (corner) {
            case TOP_LEFT, BOTTOM_LEFT -> 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - 100;
        };
    }

    private int getOverlayY(int screenHeight, com.criticalrange.config.VulkanModExtraConfig.OverlayCorner corner) {
        return switch (corner) {
            case TOP_LEFT, TOP_RIGHT -> 2;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - 30;
        };
    }

    private void drawText(GuiGraphics guiGraphics, String text, int x, int y,
                         com.criticalrange.config.VulkanModExtraConfig.TextContrast contrast) {
        int color = 0xFFFFFF; // White text

        switch (contrast) {
            case BACKGROUND -> {
                // Draw background
                guiGraphics.fill(x - 1, y - 1, x + minecraft.font.width(text) + 1, y + 9, 0x80000000);
            }
            case SHADOW -> {
                // Draw with shadow
                guiGraphics.drawString(minecraft.font, text, x + 1, y + 1, 0x000000, false);
            }
        }

        guiGraphics.drawString(minecraft.font, text, x, y, color, false);
    }
}
