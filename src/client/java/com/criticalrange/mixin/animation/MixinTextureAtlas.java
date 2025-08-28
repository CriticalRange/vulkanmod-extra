package com.criticalrange.mixin.animation;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Performance-focused animation optimization mixin
 * Disables expensive texture animations for better performance
 */
@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas {

    @Unique
    private static final List<ResourceLocation> DISABLED_ANIMATIONS = List.of(
        // Fire animations (highest performance impact)
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/fire_0"),
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/fire_1"),
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_fire_0"),
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_fire_1"),
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/campfire_fire"),
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/campfire_log_lit"),
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_campfire_fire"),
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_campfire_log_lit"),

        // Water animations (moderate performance impact)
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still"),
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow"),

        // Lava animations (moderate performance impact)
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_still"),
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_flow"),

        // Portal animations (minimal performance impact but visually jarring)
        ResourceLocation.fromNamespaceAndPath("minecraft", "block/nether_portal")
    );

    @Redirect(method = "upload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;createTicker()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Ticker;"), require = 0)
    public TextureAtlasSprite.Ticker vulkanmodExtra$optimizeAnimatedSprites(TextureAtlasSprite instance) {
        // Use config settings to control animations
        if (instance.contents() != null) {
            ResourceLocation textureName = instance.contents().name();
            if (shouldDisableAnimation(textureName)) {
                return null; // Disable animation for performance
            }
        }
        return instance.createTicker(); // Keep animation for everything else
    }

    private boolean shouldDisableAnimation(ResourceLocation textureName) {
        String path = textureName.getPath();
        // Check config settings to determine if animation should be disabled
        if (path.contains("water") && !com.criticalrange.VulkanModExtra.CONFIG.animationSettings.water) {
            return true;
        }
        if (path.contains("lava") && !com.criticalrange.VulkanModExtra.CONFIG.animationSettings.lava) {
            return true;
        }
        if (path.contains("fire") && !com.criticalrange.VulkanModExtra.CONFIG.animationSettings.fire) {
            return true;
        }
        if (path.contains("nether_portal") && !com.criticalrange.VulkanModExtra.CONFIG.animationSettings.portal) {
            return true;
        }
        return false;
    }
}
