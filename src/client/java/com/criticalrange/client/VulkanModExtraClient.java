package com.criticalrange.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side entry point for VulkanMod Extra
 */
public class VulkanModExtraClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VulkanModExtraClientMod.initialize();
    }
}
