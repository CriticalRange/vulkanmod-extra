package com.criticalrange.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side entry point for VulkanMod Extra
 * Now uses the new refactored architecture
 */
public class VulkanModExtraClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Initialize the new refactored client system
        VulkanModExtraClientRefactored refactoredClient = new VulkanModExtraClientRefactored();
        refactoredClient.onInitializeClient();

        // Keep legacy initialization for compatibility during transition
        VulkanModExtraClientMod.initialize();
    }
}
