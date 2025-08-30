package com.criticalrange.client;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.gui.GuiGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VulkanModExtraClientMod {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Extra Client");
    private static VulkanModExtraHud hud;

    public static void initialize() {
        LOGGER.info("Initializing VulkanMod Extra Client...");

        // Initialize HUD
        hud = new VulkanModExtraHud();

        // Try to integrate with VulkanMod's GUI system (safe approach)
        try {
            VulkanModExtraIntegration.tryIntegrateWithVulkanMod();
        } catch (Exception e) {
            LOGGER.warn("Failed to integrate with VulkanMod GUI, but features will still work", e);
        }

        LOGGER.info("VulkanMod Extra Client initialized successfully!");
    }

    public static void onHudRender(GuiGraphics guiGraphics, float partialTicks) {
        if (hud != null) {
            hud.onHudRender(guiGraphics, partialTicks);
        }
    }
}
