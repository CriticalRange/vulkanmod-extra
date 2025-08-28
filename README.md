# VulkanMod Extra

A comprehensive performance and quality-of-life enhancement mod for VulkanMod.

## ✅ Current Status

**Fully Functional**: All features are working including safe GUI integration with VulkanMod's settings menu! You can control settings through the in-game menu, commands, or config files.

## ✨ Features

### 🎮 HUD Features
- **FPS Display**: Shows current FPS in the corner of the screen
- **Extended FPS**: Shows additional FPS information
- **Coordinates**: Displays player coordinates on screen
- **Toast Controls**: Individual control over different toast notifications:
  - Global toast toggle
  - Advancement toasts
  - Recipe toasts
  - System toasts
  - Tutorial toasts

### ⚡ Performance Features
- **Instant Sneak**: Instant camera transitions when sneaking
- **Adaptive Sync**: Smart VSync optimization
- **Steady Debug HUD**: Consistent debug screen updates
- **Light Updates**: Throttled light calculations for better performance

### 🎨 Animation & Particles
- **Texture Animations**: Control water, lava, fire, and portal animations
- **Particle Effects**: Control various particle types (flame, smoke, rain, etc.)
- **Biome Colors**: Control grass, foliage, and water colors

### 🛠️ Other Features
- **Shader Prevention**: Skip expensive shader loading
- **Mac Resolution Reduction**: Automatic resolution optimization on macOS

## 📋 Commands

### Status Check
```
/vulkanmod-extra
```
Shows the current status of all features with color-coded enabled/disabled indicators.

### Config Reload (OP Required)
```
/vulkanmod-extra reload
```
Reloads the configuration file (requires OP level 2).

## ⚙️ Configuration

All settings can be controlled via the config file:
```
.minecraft/config/vulkanmod-extra-options.json
```

### Example Configuration
```json
{
  "extra_settings": {
    "overlay_corner": "TOP_LEFT",
    "text_contrast": "NONE",
    "show_fps": false,
    "show_f_p_s_extended": true,
    "show_coords": false,
    "reduce_resolution_on_mac": false,
    "use_adaptive_sync": false,
    "toasts": true,
    "advancement_toast": true,
    "recipe_toast": true,
    "system_toast": true,
    "tutorial_toast": true,
    "instant_sneak": false,
    "prevent_shaders": false,
    "steady_debug_hud": true,
    "steady_debug_hud_refresh_interval": 1
  },
  "animation_settings": {
    "animation": false,
    "water": false,
    "lava": false,
    "fire": false,
    "portal": false,
    "block_animations": false,
    "sculk_sensor": false
  },
  "particle_settings": {
    "particles": false,
    "rain_splash": false,
    "block_break": false,
    "block_breaking": false,
    "flame": false,
    "smoke": false,
    "campfire_smoke": false,
    "bubble": false,
    "splash": false,
    "rain": false,
    "dripping_water": false
  }
}
```

## 🚀 Installation

1. Download the mod JAR file
2. Place it in your `.minecraft/mods` folder
3. Launch Minecraft with VulkanMod installed
4. **Access Settings**: Open VulkanMod's settings menu to see VulkanMod Extra pages
5. **Alternative**: Use `/vulkanmod-extra` command to check feature status
6. **Advanced**: Edit `.minecraft/config/vulkanmod-extra-options.json` directly

## 🔧 Troubleshooting

### GUI Settings Not Appearing
1. Make sure VulkanMod is installed and working
2. Restart Minecraft after installing VulkanMod Extra
3. Check that both mods appear in the mod list
4. Look for "VulkanMod Extra" pages in VulkanMod's settings menu
5. If pages don't appear, use commands or config files as alternatives

### Features Not Working
1. Check feature status with `/vulkanmod-extra`
2. Edit the config file directly: `.minecraft/config/vulkanmod-extra-options.json`
3. Use `/vulkanmod-extra reload` to reload configuration
4. Restart Minecraft if changes don't take effect

### Commands Not Working
1. Make sure you're in-game (not in main menu)
2. Check that the mod is loaded in the mod list
3. Try `/vulkanmod-extra` without parameters

## 📝 Notes

- All features are fully functional with safe GUI integration working
- Configuration is automatically saved when changed
- Use `/vulkanmod-extra` command to verify feature states
- Config changes require a restart for some features to take effect
- Control settings through VulkanMod's menu, commands, or config files
- GUI integration uses a safe approach that won't cause crashes

## 🎯 Future Plans

- Add more performance optimizations
- Implement additional visual enhancements
- Create keybind system for quick toggles
- Add more customization options
