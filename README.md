# VulkanMod Extra

A performance mod for VulkanMod that lets you turn off all the shiny stuff that's probably murdering your FPS. Think of it as [Sodium Extra](https://github.com/FlashyReese/sodium-extra-fabric) (props to them, by the way) but for VulkanMod - except with way more options to disable things you didn't even know were animated.

## Features & Optimizations

VulkanMod Extra integrates seamlessly with VulkanMod's settings GUI, giving you precise control over what gets rendered and what doesn't.

### Animations
Turn off those wobbly water textures that are probably eating your FPS alive, or keep the cool portal swirls because let's be honest, they're pretty neat. Got a potato PC? Disable everything. Got a beast rig but hate how lava looks? Just turn off lava animations. It's your world, animate it however you want.

### Particles  
Tired of your screen looking like a fireworks show every time you mine? Turn off block break particles. Want to keep the satisfying poof when you punch a creeper but lose the million rain drops? Done. There's literally a setting for angry villager particles - because apparently that was important enough to code separately (I'm talking about you, mojang!).

### Details
Don't like the sky? Turn it off. Sick of seeing stars? Gone. Want rain but hate how it looks? Keep the mechanic, ditch the visuals. You can even remove the sun and moon if you're going for that "void world but not really" aesthetic. Why would you do this? I don't know, but you can.

### Render
Fog controls that actually make sense (revolutionary, I know), lighting optimizations, and the ability to hide item frames. Also includes beacon beam height limiting because apparently some people build too many beacons and then complain about performance.

### Extra Features
FPS counter (because we all need to know exactly how much our modpacks are destroying our framerate), coordinate display for when you're lost again, and instant sneak for builders who don't have time for smooth camera transitions. Plus the ability to turn off those annoying "you got wood!" achievement popups.

## Configuration System

All settings are automatically saved to `.minecraft/config/vulkanmod-extra-options.json` and can be configured through VulkanMod's GUI or by editing the file directly.

* Configuration Template as of [v0.2.0-beta4](https://modrinth.com/mod/vulkanmod-extra/version/0.2.0-beta4+1.21.1):

```json
{
  "coreSettings": {
    "enableMod": true,
    "enableGuiIntegration": true,
    "enableDebugLogging": false,
    "autoSaveConfig": true,
    "configSaveInterval": 300
  },
  "animationSettings": {
    "water": false,
    "waterStill": false,
    "waterFlow": false,
    "lava": false,
    "lavaStill": false,
    "lavaFlow": false,
    "fire": false,
    ...
  },
  "particleSettings": {
    "rainSplash": true,
    "blockBreak": true,
    "blockBreaking": true,
    "ambientEntityEffect": true,
    "angryVillager": true,
    "ash": true,
    "barrier": true,
    "block": true,
    "blockdust": true,
    "blockMarker": true,
    "bubble": true,
    "bubbleColumnUp": true,
    "bubblePop": true,
    "campfireCosySmoke": true,
    "campfireSignalSmoke": true,
    "cherryLeaves": true,
    "cloud": true,
    ...
  },
  "renderSettings": {
    "lightUpdates": true,
    "itemFrame": true,
    "armorStand": true,
    "painting": true,
    "piston": true,
    "beaconBeam": true,
    "limitBeaconBeamHeight": false,
    "enchantingTableBook": true,
    "itemFrameNameTag": true,
    "playerNameTag": true,
    "globalFog": true,
    "fogTypeConfig": {
      "WATER": {
        "enable": true,
        "environmentStartMultiplier": 100,
        "environmentEndMultiplier": 100,
        "renderDistanceStartMultiplier": 100,
        "renderDistanceEndMultiplier": 100,
        "skyEndMultiplier": 100,
        "cloudEndMultiplier": 100
      },
      "LAVA": {
        "enable": true,
        "environmentStartMultiplier": 100,
        "environmentEndMultiplier": 100,
        "renderDistanceStartMultiplier": 100,
        "renderDistanceEndMultiplier": 100,
        "skyEndMultiplier": 100,
        "cloudEndMultiplier": 100
      },
      "POWDER_SNOW": {
        "enable": true,
        "environmentStartMultiplier": 100,
        "environmentEndMultiplier": 100,
        "renderDistanceStartMultiplier": 100,
        "renderDistanceEndMultiplier": 100,
        "skyEndMultiplier": 100,
        "cloudEndMultiplier": 100
      }
    },
    "multiDimensionFog": false
  },
  "hudSettings": {
    "overlayCorner": "TOP_LEFT",
    "textContrast": "BACKGROUND",
    "showFps": true,
    "fpsDisplayMode": "DETAILED",
    "showFPSExtended": true,
    "showCoords": true,
    "toasts": true,
    "advancementToast": true,
    "recipeToast": true,
    "systemToast": true,
    "tutorialToast": true
  },
  "performanceSettings": {
    "instantSneak": true,
    "useAdaptiveSync": true,
    "steadyDebugHud": true,
    "steadyDebugHudRefreshInterval": 1,
    "reduceResolutionOnMac": false,
    "preventShaders": false,
    "optimizeConfigWrites": true,
    "useFastRandom": false,
    "linearFlatColorBlender": false,
    "advancedItemTooltips": false
  },
  "environmentSettings": {
    "sky": true,
    "sun": true,
    "moon": true,
    "stars": true,
    "rainSnow": true,
    "biomeColors": true,
    "skyColors": true,
    "cloudHeight": 192,
    "cloudDistance": 100
  },
  "extraSettings": {
    "overlayCorner": "TOP_LEFT",
    "textContrast": "NONE",
    "showFps": false,
    "fpsDisplayMode": "BASIC",
    "showFPSExtended": true,
    "showCoords": false,
    "reduceResolutionOnMac": false,
    "useAdaptiveSync": false,
    "cloudHeight": 192,
    "cloudDistance": 100,
    "toasts": true,
    "advancementToast": true,
    "recipeToast": true,
    "systemToast": true,
    "tutorialToast": true,
    "instantSneak": false,
    "preventShaders": false,
    "steadyDebugHud": true,
    "steadyDebugHudRefreshInterval": 1,
    "enableVulkanModGuiIntegration": true,
    "optimizeConfigWrites": true,
    "advancedItemTooltips": false,
    "useFastRandom": false,
    "linearFlatColorBlender": false
  }
}
```

## Requirements & Installation

- **Minecraft**: 1.21.1
- **Fabric Loader**: 0.17.2+
- **Fabric API**: 0.116.5+
- **VulkanMod**: 0.5.5+ (Required)

### Installation Steps
1. Download the latest version from [Modrinth](https://modrinth.com/mod/vulkanmod-extra/versions)
2. Install VulkanMod and VulkanMod Extra in your `.minecraft/mods/` folder
3. Launch Minecraft with Fabric
4. Settings will appear automatically in VulkanMod's options menu

## Troubleshooting

This will be added later.

## Roadmap

- [ ] Performance presets (Potato, Balanced, Quality)
- [ ] Chunk rendering optimizations (Especially that lag spike)
- [ ] Memory usage profiling tools
- [ ] Compatibility with more mods
- [ ] Maybe shader support?

## Contributing & Support

Found a bug or have a feature request? We'd love to hear from you! Feel free to open an issue on GitHub or contribute to the project. Whether it's code, testing, or just feedback, every contribution helps make VulkanMod Extra better for everyone.

For support, check the GitHub issues page or join the discussion. We're a friendly community and always happy to help troubleshoot performance issues or explain how features work.

---

**Note**: VulkanMod Extra is designed to work specifically with VulkanMod and won't function without it