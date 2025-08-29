package com.criticalrange.mixin.animation;

import com.criticalrange.VulkanModExtra;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Texture animation control mixin based on Sodium Extra pattern
 * Controls texture animations for better performance
 */
@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas extends AbstractTexture {
    
    @Unique
    private final Map<Supplier<Boolean>, List<ResourceLocation>> animatedSprites = Map.of(
            () -> VulkanModExtra.CONFIG.animationSettings.water, List.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow")
            ),
            () -> VulkanModExtra.CONFIG.animationSettings.lava, List.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_still"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_flow")
            ),
            () -> VulkanModExtra.CONFIG.animationSettings.portal, List.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/nether_portal")
            ),
            () -> VulkanModExtra.CONFIG.animationSettings.fire, List.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/fire_0"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/fire_1"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_fire_0"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_fire_1"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/campfire_fire"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/campfire_log_lit"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_campfire_fire"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_campfire_log_lit")
            )
    );

    @Redirect(method = "upload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;createTicker()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Ticker;"))
    public TextureAtlasSprite.Ticker vulkanmodExtra$tickAnimatedSprites(TextureAtlasSprite instance) {
        TextureAtlasSprite.Ticker tickableAnimation = instance.createTicker();
        if (tickableAnimation != null && VulkanModExtra.CONFIG.animationSettings.animations && this.shouldAnimate(instance.contents().name()))
            return tickableAnimation;
        return null;
    }

    @Unique
    private boolean shouldAnimate(ResourceLocation identifier) {
        if (identifier != null) {
            for (Map.Entry<Supplier<Boolean>, List<ResourceLocation>> supplierListEntry : this.animatedSprites.entrySet()) {
                if (supplierListEntry.getValue().contains(identifier)) {
                    return supplierListEntry.getKey().get();
                }
            }
        }
        return true;
    }
}
