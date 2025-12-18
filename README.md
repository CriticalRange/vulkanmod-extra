# VulkanMod Extra Android Fix
This Fork of VulkanMod Extra intends to increase function of VulkanMod Extra on Android devices

Current state:
- opening video options no longer crashes the game 
- appears fixed

# VulkanMod Extra

[![Modrinth Version](https://img.shields.io/modrinth/v/vulkanmod-extra?logo=modrinth&label=modrinth&color=00AF5C)](https://modrinth.com/mod/vulkanmod-extra)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/vulkanmod-extra?logo=modrinth&label=downloads&color=00AF5C)](https://modrinth.com/mod/vulkanmod-extra)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1342527?logo=curseforge&label=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/vulkanmod-extra)
![GitHub Release](https://img.shields.io/github/v/release/CriticalRange/vulkanmod-extra?include_prereleases)
[![License](https://img.shields.io/github/license/CriticalRange/vulkanmod-extra?color=blue)](https://github.com/CriticalRange/vulkanmod-extra/blob/main/LICENSE)

**The ultimate performance control suite for VulkanMod.** Turn off what you don't need, keep what you love, and squeeze every last frame out of your Minecraft experience.

---

## What is VulkanMod Extra?

VulkanMod Extra extends [VulkanMod](https://modrinth.com/mod/vulkanmod) with granular control over every visual effect in Minecraft. Built on a modern modular architecture, it seamlessly integrates into VulkanMod's settings GUI while providing **unprecedented** optimization capabilities.

Think of it as having a professional audio mixing board, but for Minecraft's graphics. Every slider, every toggle, every option is there to help you create your perfect balance between visual quality and raw performance.

## Key Features

### Animation Control
Fine-tune which textures animate and which stay static. Every animation type can be toggled independently:

- **Master Toggle** - Disable all animations with one switch
- **Fluids** - Water (still/flowing), Lava (still/flowing) animations
- **Environmental** - Fire, Portal swirls, Sculk sensor pulses  
- **Blocks** - All other animated block textures

### Particle Management
Control over 80+ individual particle types. From rain splashes to enchantment glimmers, you decide what renders:

- **Master Toggle** - One switch to rule them all
- **Weather Effects** - Rain splashes, snow particles, cloud formations
- **Block Interactions** - Breaking particles, dust, debris
- **Entity Effects** - Villager emotions, mob particles, player effects
- **Environmental** - Bubbles, smoke, ash, cherry leaves
- **Special Effects** - Portal particles, enchantments, explosions

### Rendering Optimizations
Deep control over Minecraft's rendering pipeline:

- **Entity Rendering** - Item frames, armor stands, paintings
- **Lighting System** - Dynamic light updates optimization
- **Beacon Beams** - Height limiting for performance
- **Fog Effects** - Per-environment fog control with detailed multipliers
- **Name Tags** - Toggle for players and entities

### World Details
Customize your world's atmosphere:

- **Sky Elements** - Sun, moon, stars, sky color
- **Weather Visuals** - Rain/snow rendering separate from mechanics
- **Biome Colors** - Toggle biome-specific coloring
- **Cloud Settings** - Height and render distance controls

### HUD & Interface
Enhanced display features:

- **FPS Counter** - Basic or extended metrics display
- **Coordinates** - Real-time position tracking
- **Toast Notifications** - Control achievement/recipe popups
- **Debug Info** - Steady refresh rate for F3 screen

## Performance Impact

Based on community testing and feedback:

| Feature Disabled | Average FPS Gain | Notes |
|-----------------|------------------|-------|
| All Animations | +15-25 FPS | Most impact on lower-end GPUs |
| All Particles | +10-20 FPS | Scales with particle density |
| Weather Effects | +5-10 FPS | During rain/snow |
| Fog Rendering | Not tested yet | Likely helps in water/lava |
| Entity Extras | +2-5 FPS | Item frames, armor stands |

*Results vary based on hardware, world complexity, and other mods installed. These are estimates from user reports - your results may differ.*

## Installation

### Requirements
- **Minecraft**: 1.21.1 - 1.21.5
- **Mod Loader**: Fabric Loader 0.17.2+
- **Dependencies**:
  - Fabric API (Latest for your MC version)
  - [VulkanMod](https://modrinth.com/mod/vulkanmod) 0.5.3+

### Quick Install
1. Install Fabric Loader for your Minecraft version
2. Download [VulkanMod](https://modrinth.com/mod/vulkanmod) and place in `mods` folder
3. Download [VulkanMod Extra](https://modrinth.com/mod/vulkanmod-extra) and place in `mods` folder
4. Launch Minecraft
5. Open Video Settings → VulkanMod tab to access all features

### Multi-Version Support
VulkanMod Extra supports multiple Minecraft versions through our intelligent override system:
- 1.21.1 (Stable)
- 1.21.2 (Stable)
- 1.21.3 (Stable)
- 1.21.4 (Stable)
- 1.21.5 (Beta)

Download the version matching your Minecraft installation from our [releases page](https://github.com/CriticalRange/vulkanmod-extra/releases).

## Configuration

Settings are stored in `.minecraft/config/vulkanmod-extra-options.json` and can be edited manually or through the in-game GUI.

### Configuration Structure
```json
{
  "coreSettings": {
    "enableMod": true,
    "enableGuiIntegration": true,
    "enableDebugLogging": false
  },
  "animationSettings": {
    // Individual animation toggles
  },
  "particleSettings": {
    // 80+ particle type toggles
  },
  "renderSettings": {
    // Rendering optimizations
  },
  "detailSettings": {
    // World detail controls
  },
  "extraSettings": {
    // HUD and interface options
  }
}
```

### Performance Presets (Coming Soon)
We're working on one-click optimization profiles:
- **Potato** - Maximum FPS, minimum visuals
- **Balanced** - Smart compromises for most systems
- **Quality** - Minor optimizations, preserve aesthetics
- **Ultra** - All features enabled

## Technical Details

### Architecture

VulkanMod Extra uses a modern modular architecture designed for performance and maintainability:

- **Feature Registry System** - Plugin-like architecture with dependency management
- **Event-Driven Design** - Loosely coupled components communicate via EventBus
- **Error Recovery** - Automatic error handling with multiple recovery strategies
- **Dependency Graph** - Topological sorting ensures correct initialization order
- **Configuration Manager** - JSON-based config with atomic saves and validation
- **Hybrid Integration** - Event-based VulkanMod integration with reflection fallback
- **Multi-Version Support** - Shared core with version-specific overrides

📖 **[Read the full Architecture Documentation →](ARCHITECTURE.md)**

### Build System

Multi-version support through Gradle:
```bash
# Build for specific version
./gradlew build -Pminecraft_version=1.21.4

# Or build the default version for the branch
./gradlew build
```

📚 **[See CLAUDE.md for detailed build instructions →](CLAUDE.md)**

### Performance Characteristics

- **Startup Impact**: < 100ms - Fast initialization with lazy loading
- **Memory Usage**: Minimal - Efficient config system and feature caching
- **Runtime Overhead**: Negligible when features disabled, < 1ms/tick when enabled
- **Config System**: Fast JSON serialization with atomic writes

## Compatibility

### Known Compatible Mods
- Fabric API (Required)
- VulkanMod (Required)
- ModMenu (Recommended)
- Most optimization mods

### Potential Conflicts
- Sodium/Iris (Use VulkanMod instead)
- OptiFine (Incompatible with VulkanMod)
- Canvas Renderer (Different rendering pipeline)

## Troubleshooting

### Common Issues

**VulkanMod Extra options don't appear**
- Ensure VulkanMod is installed and working
- Check that GUI integration is enabled in config
- Verify you're using compatible versions

**Settings not saving**
- Ensure valid JSON syntax if editing manually
- Try deleting config file to regenerate
- Check write permissions for config folder

**Performance degradation**
- Some features may impact FPS when enabled
- Start with all features disabled, enable gradually
- Check for mod conflicts

**Crashes on startup**
- Verify VulkanMod is functioning correctly
- Check log files in `.minecraft/logs`
- Ensure Vulkan drivers are installed

## Contributing

We welcome contributions from everyone! Whether you're a developer, translator, or just passionate about performance:

- 🐛 **Bug Reports & Fixes** - Help us squash bugs
- ✨ **Feature Development** - Implement new optimizations
- 🌍 **Translations** - 16 languages supported, more welcome!
- 📚 **Documentation** - Improve guides and tutorials
- 🧪 **Testing** - Benchmark and verify performance claims

🎯 **[Read CONTRIBUTING.md for complete guidelines →](CONTRIBUTING.md)**

### Quick Start for Contributors

1. Fork the repository
2. Read [ARCHITECTURE.md](ARCHITECTURE.md) to understand the codebase
3. Check [open issues](https://github.com/CriticalRange/vulkanmod-extra/issues) for tasks
4. Create a feature branch and make your changes
5. Submit a pull request!

**All contributors are credited in our [CHANGELOG](CHANGELOG.md)!**

## Documentation

Comprehensive documentation to help you understand, use, and contribute to VulkanMod Extra:

| Document | Purpose | Audience |
|----------|---------|----------|
| **[README.md](README.md)** | Project overview, features, installation | Everyone |
| **[CONTRIBUTING.md](CONTRIBUTING.md)** | Contribution guidelines, code style, PR process | Contributors |
| **[ARCHITECTURE.md](ARCHITECTURE.md)** | System design, core systems, data flow diagrams | Developers |
| **[CLAUDE.md](CLAUDE.md)** | Build system, development setup, internal guidelines | Developers |
| **[CHANGELOG.md](CHANGELOG.md)** | Version history, changes, release notes | Everyone |
| **[lang/README.md](common/src/main/resources/assets/vulkanmod-extra/lang/README.md)** | Translation guide, language support | Translators |

## Development Roadmap

### Current Focus (v0.2.x)
- ✅ Master toggle controls
- ✅ Enhanced GUI integration
- ✅ Multi-version support
- 🔄 Performance preset system

### Upcoming Features (v0.3.x)
- [ ] Chunk rendering optimizations
- [ ] Advanced culling algorithms
- [ ] Memory usage profiler
- [ ] Custom shader support hooks

### Future Plans (v1.0)
- [ ] Complete GUI redesign
- [ ] Advanced LOD system
- [ ] Dynamic performance scaling
- [ ] Cloud-based preset sharing

## Support & Community

Need help or want to share your experience?

- **Issues**: [GitHub Issues](https://github.com/CriticalRange/vulkanmod-extra/issues)
- **Discussions**: [GitHub Discussions](https://github.com/CriticalRange/vulkanmod-extra/discussions)
- **Discord**: Join the VulkanMod community
- **Wiki**: [Documentation](https://github.com/CriticalRange/vulkanmod-extra/wiki)

## Credits & Thanks

- **xCollateral** - Creator of VulkanMod
- **FlashyReese** - Inspiration from Sodium Extra
- **Fabric Team** - Amazing modding platform
- **Contributors** - Everyone who's helped improve the mod
- **Community** - For testing, feedback, and support

## License

VulkanMod Extra is licensed under the [MIT License](https://github.com/CriticalRange/vulkanmod-extra/blob/main/LICENSE).

You're free to:
- Use the mod in any modpack
- Modify and redistribute
- Use commercially
- Just please give credit!

---

<div align="center">

**Built with care for the Minecraft community**

*Making Minecraft run better, one frame at a time*

[Download](https://modrinth.com/mod/vulkanmod-extra) • [Report Bug](https://github.com/CriticalRange/vulkanmod-extra/issues) • [Request Feature](https://github.com/CriticalRange/vulkanmod-extra/issues)

</div>
