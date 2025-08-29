# VulkanMod Extra

A comprehensive performance and quality-of-life enhancement mod for VulkanMod with a modern, modular architecture.

## ✅ Current Status

**🆕 Recently Refactored**: Complete architectural overhaul with modular design, improved maintainability, and enhanced performance. All features are working with the new system while maintaining full compatibility.

**Fully Functional**: All features are working including safe GUI integration with VulkanMod's settings menu! You can control settings through the in-game menu, commands, or config files.

## 🏗️ Architecture

### New Modular Design
- **Feature-Based Architecture**: Each feature is now a self-contained module
- **Clean Separation of Concerns**: Configuration, features, and integration are properly separated
- **Registry Pattern**: Centralized feature management with the FeatureManager
- **Configuration Manager**: Robust configuration system with automatic backups
- **Event-Driven**: Clean event system for feature coordination

### Key Components
- `FeatureManager`: Central registry for all features
- `ConfigurationManager`: Handles all configuration operations
- `BaseFeature`: Abstract base class for implementing features
- `FeatureCategory`: Organized feature categorization
- Individual feature modules in `src/client/java/com/criticalrange/features/`

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

### New Configuration System
The refactored version uses a hierarchical, modular configuration system:

```
.minecraft/config/vulkanmod-extra/config.json
```

### Configuration Structure
```json
{
  "coreSettings": {
    "enableMod": true,
    "enableGuiIntegration": true,
    "enableDebugLogging": false,
    "autoSaveConfig": true
  },
  "hudSettings": {
    "showFps": false,
    "fpsDisplayMode": "BASIC",
    "overlayCorner": "TOP_LEFT",
    "textContrast": "NONE",
    "showCoords": false,
    "toasts": true
  },
  "performanceSettings": {
    "instantSneak": false,
    "useAdaptiveSync": false,
    "steadyDebugHud": true,
    "preventShaders": false
  },
  "animationSettings": {
    "animations": true,
    "water": true,
    "lava": true,
    "fire": true,
    "portal": true,
    "blockAnimations": true
  },
  "particleSettings": {
    "particles": true,
    "rainSplash": true,
    "flame": true,
    "smoke": true
  },
  "environmentSettings": {
    "sky": true,
    "sun": true,
    "moon": true,
    "stars": true,
    "rainSnow": true
  }
}
```

### Legacy Compatibility
The new system maintains full compatibility with existing configurations. Old config files will be automatically migrated with backups created.

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
