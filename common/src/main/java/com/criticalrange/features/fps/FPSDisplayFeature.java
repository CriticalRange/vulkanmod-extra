package com.criticalrange.features.fps;

import com.criticalrange.core.BaseFeature;
import com.criticalrange.core.FeatureCategory;
import com.criticalrange.config.VulkanModExtraConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * FPS Display feature - shows FPS information on screen
 */
public class FPSDisplayFeature extends BaseFeature {

    private FPSCounter fpsCounter;
    private boolean showDetails = true;

    public FPSDisplayFeature() {
        super("fps_display", "FPS Display", FeatureCategory.FPS,
              "Display FPS information on the screen overlay");
    }

    @Override
    public void initialize(MinecraftClient minecraft) {
        fpsCounter = new FPSCounter();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (fpsCounter != null) {
            fpsCounter.reset();
        }
    }

    @Override
    public boolean isEnabled() {
        VulkanModExtraConfig config = getConfig();
        return config != null && config.extraSettings.showFps && enabled;
    }

    @Override
    public void onTick(MinecraftClient minecraft) {
        if (fpsCounter != null && isEnabled()) {
            fpsCounter.update();
        }
    }

    /**
     * Render the FPS display
     */
    public void render(DrawContext drawContext, float partialTicks) {
        if (!isEnabled() || fpsCounter == null) {
            return;
        }

        MinecraftClient minecraft = getMinecraft();
        if (minecraft == null) {
            return;
        }

        VulkanModExtraConfig config = getConfig();
        if (config == null) {
            return;
        }

        String fpsText = buildFPSText(config);
        int color = getTextColor();

        // Position based on overlay corner setting
        int screenWidth = minecraft.getWindow().getScaledWidth();
        int screenHeight = minecraft.getWindow().getScaledHeight();
        int x = calculateXPosition(screenWidth);
        int y = calculateYPosition(screenHeight);

        drawContext.drawText(minecraft.textRenderer, fpsText, x, y, color, false);
    }

    private String buildFPSText(VulkanModExtraConfig config) {
        StringBuilder text = new StringBuilder();
        text.append("FPS: ").append(fpsCounter.getCurrentFPS());

        // Only show extended stats in full or detailed modes
        var mode = config.extraSettings.fpsDisplayMode;
        boolean showExtended = (mode == VulkanModExtraConfig.FPSDisplayMode.EXTENDED || mode == VulkanModExtraConfig.FPSDisplayMode.DETAILED);
        if (showExtended && config.extraSettings.showFPSDetails && showDetails) {
            text.append(" (")
                .append("avg: ").append(fpsCounter.getAverageFPS())
                .append(", min: ").append(fpsCounter.getMinFPS())
                .append(", max: ").append(fpsCounter.getMaxFPS())
                .append(")");
        }

        return text.toString();
    }

    private int getTextColor() {
        VulkanModExtraConfig config = getConfig();
        if (config == null) {
            return 0xFFFFFF;
        }

        return switch (config.extraSettings.textContrast) {
            case NONE -> 0xFFFFFF;
            case BACKGROUND -> 0x000000;
            case SHADOW -> 0xFFFFFF;
        };
    }

    private int calculateXPosition(int screenWidth) {
        VulkanModExtraConfig config = getConfig();
        if (config == null) {
            return 2;
        }

        return switch (config.extraSettings.overlayCorner) {
            case TOP_LEFT, BOTTOM_LEFT -> 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - 100; // Approximate text width
        };
    }

    private int calculateYPosition(int screenHeight) {
        VulkanModExtraConfig config = getConfig();
        if (config == null) {
            return 2;
        }

        return switch (config.extraSettings.overlayCorner) {
            case TOP_LEFT, TOP_RIGHT -> 2;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - 12; // Font height + padding
        };
    }

    /**
     * Set whether to show extended FPS information
     */
    public void setShowDetails(boolean showDetails) {
        this.showDetails = showDetails;
    }

    /**
     * Simple FPS counter implementation
     */
    private static class FPSCounter {
        private int fps = 0;
        private int frameCount = 0;
        private long lastTime = System.currentTimeMillis();
        private final int[] fpsHistory = new int[60]; // Last 60 seconds
        private int historyIndex = 0;
        private int minFPS = Integer.MAX_VALUE;
        private int maxFPS = 0;
        private int totalFPS = 0;

        public void update() {
            frameCount++;
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastTime >= 1000) { // Update every second
                fps = frameCount;
                frameCount = 0;
                lastTime = currentTime;

                // Update history
                fpsHistory[historyIndex] = fps;
                historyIndex = (historyIndex + 1) % fpsHistory.length;

                // Update min/max
                if (fps < minFPS) minFPS = fps;
                if (fps > maxFPS) maxFPS = fps;

                // Reset min/max every minute
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

        public void reset() {
            fps = 0;
            frameCount = 0;
            lastTime = System.currentTimeMillis();
            minFPS = Integer.MAX_VALUE;
            maxFPS = 0;
            totalFPS = 0;
            historyIndex = 0;
        }

        public int getCurrentFPS() {
            return fps;
        }

        public int getAverageFPS() {
            if (historyIndex == 0) return 0;
            return totalFPS / historyIndex;
        }

        public int getMinFPS() {
            return minFPS == Integer.MAX_VALUE ? 0 : minFPS;
        }

        public int getMaxFPS() {
            return maxFPS;
        }
    }
}
