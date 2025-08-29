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


/**
 * Texture animation control mixin based on Sodium Extra pattern
 * Controls texture animations for better performance
 */
@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas extends AbstractTexture {
    

    @Redirect(method = "upload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;createTicker()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Ticker;"))
    public TextureAtlasSprite.Ticker vulkanmodExtra$tickAnimatedSprites(TextureAtlasSprite instance) {
        TextureAtlasSprite.Ticker tickableAnimation = instance.createTicker();
        
        if (tickableAnimation != null) {
            String textureName = instance.contents().name().toString();
            boolean shouldAnimate = this.shouldAnimate(instance.contents().name());
            
            
            if (shouldAnimate) {
                return tickableAnimation;
            }
        }
        
        return null;
    }

    @Unique
    private boolean shouldAnimate(ResourceLocation identifier) {
        if (identifier == null) {
            return true;
        }
        
        // Get fresh configuration values directly from the configuration manager
        try {
            com.criticalrange.config.ConfigurationManager configManager = com.criticalrange.config.ConfigurationManager.getInstance();
            com.criticalrange.config.VulkanModExtraConfig config = configManager.getConfig();
            
            String idString = identifier.toString();
            
            // Check specific animation types based on texture path
            if (idString.contains("water_still") || idString.contains("water_flow")) {
                return config.animationSettings.water;
            }
            if (idString.contains("lava_still") || idString.contains("lava_flow")) {
                return config.animationSettings.lava;
            }
            if (idString.contains("nether_portal")) {
                return config.animationSettings.portal;
            }
            if (idString.contains("fire_0") || idString.contains("fire_1") || 
                idString.contains("soul_fire") || idString.contains("campfire")) {
                return config.animationSettings.fire;
            }
            if (idString.contains("sculk") || idString.contains("vibration")) {
                return config.animationSettings.sculkSensor;
            }
            
            // Check for general block animations
            String[] blockAnimationTextures = {
                "magma", "lantern", "sea_lantern", "kelp", "seagrass", "warped_stem", "crimson_stem",
                "blast_furnace", "smoker", "stonecutter", "prismarine", "respawn_anchor", "conduit"
            };
            
            for (String blockTexture : blockAnimationTextures) {
                if (idString.contains(blockTexture)) {
                    return config.animationSettings.blockAnimations;
                }
            }
            
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to get animation config in mixin", e);
        }
        
        // Default: allow animation for unrecognized textures
        return true;
    }
}
