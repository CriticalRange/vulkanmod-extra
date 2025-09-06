package com.criticalrange.client;

import com.criticalrange.VulkanModExtra;
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
    
    // FPS tracking for extended modes
    private int minFps = Integer.MAX_VALUE;
    private int maxFps = 0;
    private final java.util.List<Integer> fpsHistory = new java.util.ArrayList<>();
    private static final int HISTORY_SIZE = 1000; // Track last 1000 frames for percentile calculation

    public VulkanModExtraHud() {
        this.minecraft = Minecraft.getInstance();
    }

    public void onHudRender(GuiGraphics guiGraphics, float partialTicks) {
        if (minecraft.player == null) return;
        
        // Show our HUD when F3 debug screen is NOT open, or show alongside it
        // For now, let's always show it to test if it works

        // Update FPS tracking every frame for accurate percentiles
        int currentFps = minecraft.getFps();
        fpsHistory.add(currentFps);
        if (fpsHistory.size() > HISTORY_SIZE) {
            fpsHistory.remove(0);
        }
        
        // Update FPS counter and stats every second
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= 1000) { // Update every second
            fps = currentFps;
            
            // Update min/max
            if (currentFps < minFps) minFps = currentFps;
            if (currentFps > maxFps) maxFps = currentFps;
            
            lastUpdateTime = currentTime;
            
            // Debug log to see if HUD is being called and what the config values are (only once per second)
            var config = VulkanModExtra.CONFIG.extraSettings;
            var fpsMode = config.fpsDisplayMode;
            VulkanModExtra.LOGGER.debug("FPS updated - showFps: {}, fpsDisplayMode: {}, FPS: {}", config.showFps, fpsMode, fps);
        }

        renderOverlay(guiGraphics);
    }

    private void renderOverlay(GuiGraphics guiGraphics) {
        var config = VulkanModExtra.CONFIG.extraSettings;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Calculate overlay position based on corner setting
        var overlayCorner = config.overlayCorner;
        int x = getOverlayX(screenWidth, overlayCorner);
        int y = getOverlayY(screenHeight, overlayCorner);

        int lineHeight = 10;
        int currentY = y;

        // FPS display
        if (config.showFps) {
            currentY += renderFpsDisplay(guiGraphics, x, currentY, lineHeight, config);
        }

        // Coordinates display
        if (config.showCoords) {
            Player player = minecraft.player;
            String coordsText = String.format("XYZ: %.1f / %.1f / %.1f",
                player.getX(), player.getY(), player.getZ());
            var textContrast = config.textContrast;
            drawText(guiGraphics, coordsText, x, currentY, textContrast);
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
    
    private int renderFpsDisplay(GuiGraphics guiGraphics, int x, int y, int lineHeight,
                                com.criticalrange.config.VulkanModExtraConfig.ExtraSettings config) {
        int linesRendered = 0;
        var fpsMode = config.fpsDisplayMode;
        var textContrast = config.textContrast;
        
        switch (fpsMode) {
            case BASIC -> {
                String fpsText = "FPS: " + fps;
                drawText(guiGraphics, fpsText, x, y, textContrast);
                linesRendered = 1;
            }
            case EXTENDED -> {
                // Calculate average from history
                int avgFps = fpsHistory.isEmpty() ? fps : 
                    (int) fpsHistory.stream().mapToInt(Integer::intValue).average().orElse(fps);
                
                String fpsText = String.format("FPS: %d (min: %d, avg: %d, max: %d)", 
                    fps, minFps, avgFps, maxFps);
                drawText(guiGraphics, fpsText, x, y, textContrast);
                linesRendered = 1;
            }
            case DETAILED -> {
                // Calculate average from history
                int avgFps = fpsHistory.isEmpty() ? fps : 
                    (int) fpsHistory.stream().mapToInt(Integer::intValue).average().orElse(fps);
                
                // Line 1: Basic stats
                String fpsText = String.format("FPS: %d (min: %d, avg: %d, max: %d)", 
                    fps, minFps, avgFps, maxFps);
                drawText(guiGraphics, fpsText, x, y, textContrast);
                
                // Line 2: Percentiles
                int low1Percent = getPercentile(1.0);
                int low01Percent = getPercentile(0.1);
                String percentileText = String.format("Low 1%%: %d, Low 0.1%%: %d", 
                    low1Percent, low01Percent);
                drawText(guiGraphics, percentileText, x, y + lineHeight, textContrast);
                linesRendered = 2;
            }
        }
        
        return linesRendered * lineHeight;
    }
    
    private int getPercentile(double percentile) {
        if (fpsHistory.isEmpty()) return fps;
        
        java.util.List<Integer> sortedHistory = new java.util.ArrayList<>(fpsHistory);
        java.util.Collections.sort(sortedHistory);
        
        int index = (int) Math.ceil(sortedHistory.size() * percentile / 100.0) - 1;
        index = Math.max(0, Math.min(index, sortedHistory.size() - 1));
        
        return sortedHistory.get(index);
    }
}
