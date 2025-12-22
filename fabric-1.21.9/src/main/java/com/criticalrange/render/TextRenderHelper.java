package com.criticalrange.render;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.config.VulkanModExtraConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Version-specific text rendering helper for Minecraft 1.21.9
 * Provides the draw methods with correct API signatures for this version
 */
public class TextRenderHelper {

    private static int lastFPS = 0;
    private static int frameCount = 0;
    private static long lastTime = System.currentTimeMillis();
    private static final int[] fpsHistory = new int[60];
    private static int historyIndex = 0;
    private static int minFPS = Integer.MAX_VALUE;
    private static int maxFPS = 0;
    private static int totalFPS = 0;

    /**
     * Render the FPS display using 1.21.9 API
     */
    public static void renderFpsDisplay(DrawContext drawContext) {
        VulkanModExtraConfig config = VulkanModExtra.CONFIG;
        if (config == null || !config.extraSettings.showFps) {
            return;
        }

        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft == null || minecraft.textRenderer == null) {
            return;
        }

        // Update FPS
        updateFPS();

        // Build text
        String fpsText = buildFPSText(config);

        // Calculate position
        int screenWidth = minecraft.getWindow().getScaledWidth();
        int screenHeight = minecraft.getWindow().getScaledHeight();
        int x = calculateX(config, screenWidth);
        int y = calculateY(config, screenHeight);

        // Handle contrast modes - using same values as Minecraft's DebugHud
        // Background color: 0x90505050 (from DebugHud constant pool)
        var contrast = config.extraSettings.textContrast;
        
        switch (contrast) {
            case NONE -> {
                // Plain white text (ARGB: 0xFFFFFFFF)
                drawContext.drawText(minecraft.textRenderer, fpsText, x, y, 0xFFFFFFFF, false);
            }
            case BACKGROUND -> {
                // Draw background like Minecraft's debug screen (F3)
                int textWidth = minecraft.textRenderer.getWidth(fpsText);
                int fontHeight = minecraft.textRenderer.fontHeight;
                // Background: 0x90505050 = semi-transparent gray (same as DebugHud)
                drawContext.fill(x - 1, y - 1, x + textWidth + 1, y + fontHeight + 1, 0x90505050);
                // White text on top
                drawContext.drawText(minecraft.textRenderer, fpsText, x, y, 0xFFFFFFFF, false);
            }
            case SHADOW -> {
                // White text with drop shadow
                drawContext.drawText(minecraft.textRenderer, fpsText, x, y, 0xFFFFFFFF, true);
            }
        }
    }

    private static void updateFPS() {
        frameCount++;
        long now = System.currentTimeMillis();
        if (now - lastTime >= 1000) {
            lastFPS = frameCount;
            frameCount = 0;
            lastTime = now;

            fpsHistory[historyIndex] = lastFPS;
            historyIndex = (historyIndex + 1) % fpsHistory.length;

            if (lastFPS < minFPS) minFPS = lastFPS;
            if (lastFPS > maxFPS) maxFPS = lastFPS;

            if (historyIndex == 0) {
                minFPS = Integer.MAX_VALUE;
                maxFPS = 0;
                totalFPS = 0;
                for (int f : fpsHistory) {
                    if (f < minFPS) minFPS = f;
                    if (f > maxFPS) maxFPS = f;
                    totalFPS += f;
                }
            }
        }
    }

    private static String buildFPSText(VulkanModExtraConfig config) {
        StringBuilder text = new StringBuilder();
        text.append("FPS: ").append(lastFPS);

        var mode = config.extraSettings.fpsDisplayMode;
        
        // Use ordinal comparison to avoid classloader issues with enum ==
        // BASIC=0, EXTENDED=1, DETAILED=2
        int ordinal = mode != null ? mode.ordinal() : -1;
        boolean extended = (ordinal >= 1);
        
        // Show extended stats when in EXTENDED or DETAILED mode
        if (extended) {
            int avg = historyIndex == 0 ? 0 : totalFPS / historyIndex;
            int min = minFPS == Integer.MAX_VALUE ? 0 : minFPS;
            text.append(" (avg: ").append(avg)
                .append(", min: ").append(min)
                .append(", max: ").append(maxFPS).append(")");
        }
        return text.toString();
    }

    private static int calculateX(VulkanModExtraConfig config, int screenWidth) {
        return switch (config.extraSettings.overlayCorner) {
            case TOP_LEFT, BOTTOM_LEFT -> 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - 100; // Approximate text width
        };
    }

    private static int calculateY(VulkanModExtraConfig config, int screenHeight) {
        return switch (config.extraSettings.overlayCorner) {
            case TOP_LEFT, TOP_RIGHT -> 2;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - 11;
        };
    }
}
