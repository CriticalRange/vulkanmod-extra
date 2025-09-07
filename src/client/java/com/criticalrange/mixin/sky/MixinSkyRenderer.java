package com.criticalrange.mixin.sky;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Sky gradient control mixin - currently disabled due to mapping issues
 * The sky option will control the entire sky until proper injection points are found
 */
@Mixin(WorldRenderer.class)
public class MixinSkyRenderer {
    // Temporarily disabled - targeting specific sky gradient methods is not possible
    // without proper mappings for the current Minecraft version
}
