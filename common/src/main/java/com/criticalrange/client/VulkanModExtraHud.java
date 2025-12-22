package com.criticalrange.client;

import com.criticalrange.features.fps.FPSDisplayFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VulkanMod Extra HUD - Manages overlay rendering for FPS and other information
 */
public class VulkanModExtraHud {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra HUD");
    
    private final FPSDisplayFeature fpsDisplay;
    private boolean initialized = false;

    public VulkanModExtraHud() {
        this.fpsDisplay = new FPSDisplayFeature();
        
        try {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            if (minecraft != null) {
                fpsDisplay.initialize(minecraft);
                fpsDisplay.setEnabled(true);
                initialized = true;
                LOGGER.info("VulkanMod Extra HUD initialized");
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize HUD, will retry later", e);
        }
    }

    /**
     * Called every frame to render HUD elements
     */
    public void onHudRender(DrawContext drawContext, float partialTicks) {
        if (drawContext == null) {
            return;
        }

        // Lazy initialization if not done yet
        if (!initialized) {
            try {
                MinecraftClient minecraft = MinecraftClient.getInstance();
                if (minecraft != null) {
                    fpsDisplay.initialize(minecraft);
                    fpsDisplay.setEnabled(true);
                    initialized = true;
                }
            } catch (Exception e) {
                // Still not ready, skip this frame
                return;
            }
        }

        try {
            // Render FPS display
            if (fpsDisplay != null && fpsDisplay.isEnabled()) {
                fpsDisplay.render(drawContext, partialTicks);
            }
        } catch (Exception e) {
            // Silently ignore render errors to prevent spam
        }
    }

    /**
     * Get the FPS display feature
     */
    public FPSDisplayFeature getFpsDisplay() {
        return fpsDisplay;
    }

    /**
     * Check if HUD is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
