package com.criticalrange;

import com.criticalrange.client.VulkanModExtraClientMod;
import net.fabricmc.api.ClientModInitializer;

public class VulkanModExtraClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		VulkanModExtraClientMod.initialize();
	}
}