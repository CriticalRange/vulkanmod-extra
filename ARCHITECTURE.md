# VulkanMod Extra Architecture

[![Architecture Version](https://img.shields.io/badge/architecture-v2.0-blue)](https://github.com/CriticalRange/vulkanmod-extra)
[![Code Style](https://img.shields.io/badge/code%20style-standard-brightgreen)](CONTRIBUTING.md)

**A deep dive into the design and implementation of VulkanMod Extra.** This document explains the architectural decisions, core systems, and patterns that make this mod powerful, maintainable, and extensible.

---

## Table of Contents

- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Core Systems](#core-systems)
  - [Feature System](#feature-system)
  - [Dependency Management](#dependency-management)
  - [Event Bus](#event-bus)
  - [Error Recovery](#error-recovery)
  - [Configuration System](#configuration-system)
- [Integration Layer](#integration-layer)
- [Multi-Version Strategy](#multi-version-strategy)
- [Mixin Organization](#mixin-organization)
- [Data Flow](#data-flow)
- [Design Patterns](#design-patterns)
- [Performance Considerations](#performance-considerations)

---

## Overview

VulkanMod Extra is built on a **modular plugin architecture** where features are self-contained, dependencies are explicit, and integration is event-driven. This design enables:

- ✅ **Hot-swappable features** - Enable/disable at runtime
- ✅ **Graceful degradation** - System continues if a feature fails
- ✅ **Clear separation of concerns** - Each system has a single responsibility
- ✅ **Easy testing** - Components are loosely coupled
- ✅ **Multi-version support** - Shared core with version-specific overrides

### Design Philosophy

1. **Features are plugins** - Each feature is an independent module
2. **Fail safely** - Errors in one feature don't crash the game
3. **Explicit dependencies** - Dependencies are declared, not assumed
4. **Event-driven** - Components communicate through events, not direct calls
5. **Configuration-first** - All user settings flow through a central config

---

## System Architecture

### High-Level Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                          Minecraft Client                        │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                       VulkanMod (Host)                           │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │           VulkanMod Extra Integration Layer                 │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  │ │
│  │  │ Event-Based  │  │  Reflection  │  │  GUI Injection  │  │ │
│  │  │ Integration  │  │   Fallback   │  │   (VulkanMod)   │  │ │
│  │  └──────────────┘  └──────────────┘  └─────────────────┘  │ │
│  └────────────────────────────────────────────────────────────┘ │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                    VulkanMod Extra Core                          │
│                                                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Feature Manager                         │  │
│  │  • Feature Registry     • Health Monitoring               │  │
│  │  • Lifecycle Control    • Diagnostics                     │  │
│  └───────────────────────────────────────────────────────────┘  │
│                           │                                       │
│         ┌─────────────────┼─────────────────┐                   │
│         ↓                 ↓                 ↓                     │
│  ┌────────────┐   ┌────────────┐   ┌────────────┐              │
│  │ Dependency │   │  Event Bus │   │   Error    │              │
│  │   Graph    │   │            │   │  Recovery  │              │
│  └────────────┘   └────────────┘   └────────────┘              │
│         │                 │                 │                     │
│         └─────────────────┴─────────────────┘                   │
│                           │                                       │
│                           ↓                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                   Feature Layer                            │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐    │  │
│  │  │Animation │ │ Particle │ │ Rendering│ │  Details │    │  │
│  │  │ Feature  │ │ Feature  │ │ Feature  │ │ Feature  │ ...│  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘    │  │
│  └───────────────────────────────────────────────────────────┘  │
│                           │                                       │
│                           ↓                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              Configuration Manager                         │  │
│  │  • Load/Save     • Validation      • Migration            │  │
│  └───────────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                         Mixin Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Common     │  │   Version    │  │   VulkanMod  │          │
│  │   Mixins     │  │   Specific   │  │   Mixins     │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Core Systems

### Feature System

The feature system is the heart of VulkanMod Extra. It provides a plugin-like architecture where each feature is independent and self-managing.

#### Feature Interface

```java
public interface Feature {
    // Metadata
    String getId();
    String getName();
    String getVersion();
    FeatureCategory getCategory();

    // Lifecycle
    void initialize(MinecraftClient minecraft);
    void onEnable();
    void onDisable();
    void onTick(MinecraftClient minecraft);

    // Health & Dependencies
    boolean performHealthCheck();
    List<FeatureDependency> getDependencies();
}
```

#### Feature Lifecycle

```
┌─────────────┐
│  Created    │  Feature instantiated
└──────┬──────┘
       │
       ↓
┌─────────────┐
│ Registered  │  Added to FeatureManager
└──────┬──────┘
       │
       ↓ (Dependencies resolved)
┌─────────────┐
│ Initialized │  initialize() called
└──────┬──────┘
       │
       ↓ (Config enabled = true)
┌─────────────┐
│   Enabled   │◄───┐ onEnable() called
└──────┬──────┘    │
       │           │ Config toggle
       ↓           │
┌─────────────┐    │
│   Running   │    │ onTick() called each frame
└──────┬──────┘    │
       │           │
       ↓ (Config enabled = false)
┌─────────────┐    │
│  Disabled   │────┘ onDisable() called
└──────┬──────┘
       │
       ↓ (Game shutdown)
┌─────────────┐
│ Destroyed   │  Cleanup complete
└─────────────┘
```

#### BaseFeature Abstract Class

All features extend `BaseFeature`, which provides:
- Automatic error handling with ErrorRecoveryManager
- Built-in health check implementation
- Event bus integration
- Logger instance
- State management

**Example:**

```java
public class AnimationFeature extends BaseFeature {
    private final AnimationController controller;

    public AnimationFeature() {
        super(
            "animation_control",
            "Animation Control",
            "1.0.0",
            FeatureCategory.ANIMATION,
            "VulkanModExtra"
        );
        this.controller = new AnimationController();
    }

    @Override
    public void initialize(MinecraftClient minecraft) {
        // Initialize resources
        controller.init();
        LOGGER.info("Animation feature initialized");
    }

    @Override
    public void onEnable() {
        // Start controlling animations
        controller.enable();
    }

    @Override
    public void onDisable() {
        // Stop controlling animations
        controller.disable();
    }

    @Override
    public void onTick(MinecraftClient minecraft) {
        // Update animation state
        if (controller.needsUpdate()) {
            controller.update();
        }
    }
}
```

#### FeatureManager

Central registry managing all features:

```
┌────────────────────────────────────────────────────────────┐
│                      FeatureManager                         │
├────────────────────────────────────────────────────────────┤
│  Features Map:                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ "animation_control" → AnimationFeature               │  │
│  │ "particle_control"  → ParticleFeature                │  │
│  │ "fps_display"       → FPSDisplayFeature              │  │
│  │ ...                                                   │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  Operations:                                                 │
│  • registerFeature(Feature)     → Add to registry          │
│  • getFeature(String id)        → Lookup by ID             │
│  • getAllFeatures()             → Get all registered       │
│  • initializeAll()              → Initialize in dep order  │
│  • performHealthChecks()        → Check all feature health │
│  • getSystemDiagnostics()       → System status report     │
└────────────────────────────────────────────────────────────┘
```

**Usage:**

```java
// Register a feature
FeatureManager.getInstance().registerFeature(new AnimationFeature());

// Get a feature
Feature feature = FeatureManager.getInstance().getFeature("animation_control");

// Initialize all features
FeatureManager.getInstance().initializeAll(minecraft);

// Health check
Map<String, Boolean> health = FeatureManager.getInstance().performHealthChecks();
```

---

### Dependency Management

The **DependencyGraph** ensures features are initialized in the correct order based on their dependencies.

#### Dependency Types

```java
public enum DependencyType {
    REQUIRED,        // Must be present and enabled
    OPTIONAL,        // Use if present, skip if not
    CONFLICTS_WITH   // Cannot coexist with another feature
}
```

#### Dependency Graph Structure

```
Example Dependency Chain:

┌──────────────┐
│  VulkanMod   │ (External dependency)
└──────┬───────┘
       │ REQUIRED
       ↓
┌──────────────┐
│   RenderMod  │
└──────┬───────┘
       │ REQUIRED
       ↓
┌──────────────┐       ┌──────────────┐
│ BeaconBeam   │◄──────│ParticleCtrl  │ CONFLICTS_WITH
└──────┬───────┘       └──────────────┘
       │ OPTIONAL
       ↓
┌──────────────┐
│ FogControl   │
└──────────────┘

Initialization Order (topological sort):
1. VulkanMod (external)
2. RenderMod
3. BeaconBeam
4. FogControl
5. ParticleCtrl (if BeaconBeam not present)
```

#### Dependency Declaration

```java
@Override
public List<FeatureDependency> getDependencies() {
    return List.of(
        new FeatureDependency("vulkanmod", DependencyType.REQUIRED),
        new FeatureDependency("animation_control", DependencyType.OPTIONAL),
        new FeatureDependency("legacy_renderer", DependencyType.CONFLICTS_WITH)
    );
}
```

#### Cycle Detection

The dependency graph detects and prevents circular dependencies:

```
❌ Invalid (Cycle):
A → B → C → A

✅ Valid (DAG):
A → B → D
  ↘ C ↗
```

**Error Handling:**

```java
try {
    dependencyGraph.validateDependencies();
} catch (CyclicDependencyException e) {
    LOGGER.error("Cyclic dependency detected: {}", e.getCycle());
    // Disable offending features
}
```

---

### Event Bus

The **EventBus** provides loosely-coupled communication between components.

#### Event Flow

```
┌──────────────┐
│   Publisher  │
│  (Feature A) │
└──────┬───────┘
       │ 1. Post event
       ↓
┌─────────────────────────────────────────────┐
│             Event Bus                        │
│  ┌────────────────────────────────────────┐ │
│  │  Event Queue (Priority-based)          │ │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐  │ │
│  │  │ HIGH │→│ MED  │→│ LOW  │→│ LOW  │  │ │
│  │  └──────┘ └──────┘ └──────┘ └──────┘  │ │
│  └────────────────────────────────────────┘ │
└──────┬───────────────────────┬──────────────┘
       │ 2. Dispatch           │
       ↓                       ↓
┌──────────────┐       ┌──────────────┐
│ Subscriber 1 │       │ Subscriber 2 │
│ (Feature B)  │       │ (Feature C)  │
└──────────────┘       └──────────────┘
```

#### Event Types

```java
public enum EventType {
    FEATURE_INITIALIZED,    // Feature completed initialization
    FEATURE_ENABLED,        // Feature was enabled
    FEATURE_DISABLED,       // Feature was disabled
    CONFIG_CHANGED,         // Configuration was updated
    RELOAD_RESOURCES,       // Minecraft resources reloading
    RENDER_TICK,           // Render tick event
    ERROR_OCCURRED         // An error was caught
}
```

#### Event Handler Registration

```java
// Register handler with priority
EventBus.getInstance().register(
    EventType.CONFIG_CHANGED,
    event -> {
        // Handle config change
        reloadSettings();
    },
    EventPriority.HIGH  // Process before other handlers
);

// Fire event
EventBus.getInstance().fire(
    new Event(EventType.CONFIG_CHANGED, configData)
);
```

#### Priority System

```java
public enum EventPriority {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4),
    MONITOR(5);  // Runs last, read-only access
}
```

---

### Error Recovery

The **ErrorRecoveryManager** handles failures gracefully and attempts recovery.

#### Recovery Strategies

```
┌─────────────────────────────────────────────────────────┐
│            ErrorRecoveryManager                          │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  Error Occurs                                            │
│       ↓                                                   │
│  ┌─────────────┐                                         │
│  │  Classify   │ → Severity: INFO/WARNING/ERROR/CRITICAL│
│  └──────┬──────┘                                         │
│         ↓                                                 │
│  ┌─────────────────────────────────────────┐            │
│  │       Select Recovery Strategy          │            │
│  ├─────────────────────────────────────────┤            │
│  │  • RETRY          → Try again (max 3x)  │            │
│  │  • DISABLE_FEATURE → Turn off feature   │            │
│  │  • RESET_STATE    → Reset to default    │            │
│  │  • LOG_ONLY       → Record and continue │            │
│  │  • ESCALATE       → Report to user      │            │
│  └─────────────────────────────────────────┘            │
│         ↓                                                 │
│  ┌─────────────┐                                         │
│  │   Execute   │                                         │
│  │  Strategy   │                                         │
│  └──────┬──────┘                                         │
│         ↓                                                 │
│  ┌─────────────┐                                         │
│  │Track Stats  │ → Success/failure rate                 │
│  └─────────────┘                                         │
└─────────────────────────────────────────────────────────┘
```

#### Usage Example

```java
public void riskyOperation() {
    ErrorRecoveryManager.getInstance().handleError(
        () -> {
            // Risky code
            performComplexCalculation();
        },
        ErrorSeverity.ERROR,
        RecoveryStrategy.RETRY,
        "ComplexCalculation",
        3  // Max retries
    );
}
```

#### Error Statistics

```java
// Get recovery statistics
Map<String, RecoveryStats> stats =
    ErrorRecoveryManager.getInstance().getRecoveryStats();

// Example output:
// "animation_feature": {
//   totalErrors: 5,
//   successfulRecoveries: 4,
//   failedRecoveries: 1,
//   recoveryRate: 80%
// }
```

---

### Configuration System

The configuration system manages all user settings with JSON serialization.

#### Config Structure

```
VulkanModExtraConfig
├── animationSettings
│   ├── allAnimations: boolean
│   ├── water: boolean
│   ├── lava: boolean
│   └── ... (40+ animation toggles)
│
├── particleSettings
│   ├── allParticles: boolean
│   ├── rain: boolean
│   ├── explosion: boolean
│   └── ... (80+ particle toggles)
│
├── renderSettings
│   ├── fog: boolean
│   ├── beaconBeam: boolean
│   ├── beaconBeamHeight: int
│   └── ... (20+ render options)
│
├── detailSettings
│   ├── sky: boolean
│   ├── sun: boolean
│   └── ... (10+ detail options)
│
└── extraSettings
    ├── showFps: boolean
    ├── fpsDisplayMode: enum
    └── ... (15+ extra options)
```

#### ConfigurationManager

```
┌──────────────────────────────────────────────────────────┐
│                 ConfigurationManager                      │
├──────────────────────────────────────────────────────────┤
│                                                            │
│  Config File: .minecraft/config/vulkanmod-extra-options.json│
│                                                            │
│  Operations:                                               │
│  ┌──────────────────────────────────────────────────────┐│
│  │ loadConfig()                                          ││
│  │  1. Check if file exists                             ││
│  │  2. Parse JSON → VulkanModExtraConfig                ││
│  │  3. Validate values                                  ││
│  │  4. Apply defaults for missing fields                ││
│  │  5. Return config object                             ││
│  └──────────────────────────────────────────────────────┘│
│                                                            │
│  ┌──────────────────────────────────────────────────────┐│
│  │ saveConfig()                                          ││
│  │  1. Serialize config → JSON                          ││
│  │  2. Write atomically (temp file → rename)            ││
│  │  3. Fire CONFIG_CHANGED event                        ││
│  │  4. Trigger resource reload (if needed)              ││
│  └──────────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────────┘
```

#### Configuration Flow

```
User Changes Setting in GUI
         ↓
VulkanModPageFactory setter callback
         ↓
Update VulkanModExtra.CONFIG object
         ↓
ConfigurationManager.saveConfig()
         ↓
┌─────────────────────────────┐
│  Write to JSON file         │
│  (atomic operation)         │
└─────────────────────────────┘
         ↓
Fire CONFIG_CHANGED event
         ↓
┌─────────────────────────────┐
│ Features react to changes   │
│ - AnimationFeature updates  │
│ - ParticleFeature updates   │
│ - RenderFeature updates     │
└─────────────────────────────┘
         ↓
Resource Reload (if animations changed)
```

---

## Integration Layer

### Hybrid VulkanMod Integration

VulkanMod Extra uses a sophisticated **hybrid integration** strategy:

```
┌────────────────────────────────────────────────────────┐
│        HybridVulkanModIntegration                       │
├────────────────────────────────────────────────────────┤
│                                                          │
│  Attempt 1: Event-Based (Preferred)                    │
│  ┌────────────────────────────────────────────────┐   │
│  │  VulkanModEvents.CONFIG_PAGES_ADDING           │   │
│  │    ↓                                            │   │
│  │  Register callback                              │   │
│  │    ↓                                            │   │
│  │  VulkanMod calls our callback                   │   │
│  │    ↓                                            │   │
│  │  Add our pages to VulkanMod GUI                │   │
│  └────────────────────────────────────────────────┘   │
│         ↓ Success? → DONE                              │
│         ↓ Fail? → Fallback to Attempt 2               │
│                                                          │
│  Attempt 2: Reflection Fallback                        │
│  ┌────────────────────────────────────────────────┐   │
│  │  Find VulkanMod classes via reflection         │   │
│  │    ↓                                            │   │
│  │  Inject pages using reflection                  │   │
│  │    ↓                                            │   │
│  │  Hook into GUI initialization                   │   │
│  └────────────────────────────────────────────────┘   │
│         ↓ Success? → DONE (with warning)              │
│         ↓ Fail? → Graceful degradation                │
│                                                          │
│  Fallback: Standalone Mode                             │
│  ┌────────────────────────────────────────────────┐   │
│  │  Log integration failure                        │   │
│  │  Features still work                            │   │
│  │  GUI not available in VulkanMod settings        │   │
│  └────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘
```

### VulkanMod Page Factory

Creates option pages dynamically:

```java
// Page structure
OptionPage
  └─ OptionBlock[] blocks
       └─ Option[] options
            ├─ SwitchOption (boolean)
            ├─ CyclingOption (enum)
            └─ RangeOption (int/float)

// Dynamic creation
PageType.ANIMATION →
  • Master Controls block
     └─ allAnimations toggle
  • Fluid Animations block
     └─ water, lava, waterStill, etc.
  • Fire & Light block
     └─ fire, lantern, campfire, etc.
  ... (10 total blocks)
```

### Translation Integration

All GUI text uses Minecraft's translation system:

```
Text.translatable("vulkanmod-extra.option.render.fog")
         ↓
Minecraft Language System
         ↓
Load: assets/vulkanmod-extra/lang/{language}.json
         ↓
Display: "Fog" (en_us) or "霧" (ja_jp) or "Туман" (ru_ru)
```

---

## Multi-Version Strategy

### Version Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Version Strategy                      │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  common/  (Version-agnostic code)                        │
│  ├─ core/              → Feature system                  │
│  ├─ config/            → Configuration                   │
│  ├─ integration/       → VulkanMod integration           │
│  ├─ mixins/            → Common mixins                   │
│  │   ├─ animations/    → TextureAtlas mixins            │
│  │   ├─ particles/     → ParticleEngine mixins          │
│  │   └─ render/        → Common render mixins           │
│  └─ features/          → Feature implementations         │
│                                                           │
│  fabric-1.21.1/  (Version-specific overrides)           │
│  ├─ mixins/vulkanmod/  → VulkanMod v0.5.3 mixins        │
│  ├─ mixins/render/     → MC 1.21.1 render mixins        │
│  └─ build.gradle       → Version dependencies            │
│                                                           │
│  fabric-1.21.2/  (Version-specific overrides)           │
│  ├─ mixins/vulkanmod/  → VulkanMod v0.5.4 mixins        │
│  └─ ...                                                  │
└─────────────────────────────────────────────────────────┘
```

### Shared Code Pattern

```java
// common/  - Shared interface
public interface FogController {
    void setFogEnabled(boolean enabled);
    float getFogDensity();
}

// fabric-1.21.1/  - MC 1.21.1 implementation
public class FogController_1_21_1 implements FogController {
    @Override
    public void setFogEnabled(boolean enabled) {
        // MC 1.21.1 specific code
        minecraft.worldRenderer.setFogEnabled(enabled);
    }
}

// fabric-1.21.2/  - MC 1.21.2 implementation
public class FogController_1_21_2 implements FogController {
    @Override
    public void setFogEnabled(boolean enabled) {
        // MC 1.21.2 changed the API
        minecraft.worldRenderer.fogSettings.enabled = enabled;
    }
}
```

---

## Mixin Organization

### Mixin Categories

```
Mixins
├── Common (all versions)
│   ├── animations/
│   │   └── MixinTextureAtlas  → Intercept texture loading
│   ├── particles/
│   │   └── MixinParticleEngine  → Control particle spawning
│   ├── render/
│   │   ├── MixinLevelRenderer  → Sky, weather rendering
│   │   └── MixinLevelLightEngine  → Light updates
│   └── extra/
│       └── MixinWindow  → Window/display control
│
└── Version-Specific
    ├── fabric-1.21.1/
    │   ├── vulkanmod/
    │   │   ├── MixinVulkanModOptions  → Integrate with VulkanMod 0.5.3
    │   │   └── MixinVulkanModGraphics  → Graphics settings
    │   ├── render/
    │   │   └── MixinFogRenderer  → MC 1.21.1 fog API
    │   └── extra/
    │       └── MixinDebugHud  → MC 1.21.1 debug screen
    │
    └── fabric-1.21.2/
        └── ... (same structure, different implementations)
```

### Mixin Best Practices

```java
// ✅ Good: Minimal, focused mixin
@Mixin(TextureAtlas.class)
public class MixinTextureAtlas {
    @Inject(method = "upload", at = @At("HEAD"), cancellable = true)
    private void onUpload(CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.animationSettings.allAnimations) {
            // Skip animation upload
            ci.cancel();
        }
    }
}

// ❌ Bad: Too broad, modifies too much
@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "*", at = @At("*"))
    private void interceptEverything() { ... }
}
```

---

## Data Flow

### Complete User Action Flow

```
User clicks "Disable Fog" toggle in GUI
         ↓
VulkanModPageFactory.createSwitchOption()
         ↓
Setter callback: (value) -> setBooleanField(config, "fog", value)
         ↓
VulkanModExtra.CONFIG.renderSettings.fog = false
         ↓
ConfigurationManager.saveConfig()
         ↓
JSON file written: vulkanmod-extra-options.json
         ↓
EventBus.fire(CONFIG_CHANGED)
         ↓
┌────────────────────────────────────────┐
│  Subscribers react:                    │
│  • RenderFeature.onConfigChange()      │
│    └─> Updates fog state               │
│  • LoggingFeature.onConfigChange()     │
│    └─> Logs change                     │
└────────────────────────────────────────┘
         ↓
Next render frame
         ↓
MixinFogRenderer.onSetupFog()
         ↓
if (!VulkanModExtra.CONFIG.renderSettings.fog) {
    ci.cancel();  // Skip fog rendering
}
         ↓
Result: No fog rendered, FPS improves
```

---

## Design Patterns

### 1. Plugin Pattern (Features)

Each feature is a plugin that can be loaded/unloaded independently.

**Benefits:**
- Easy to add new features
- Features can be developed in isolation
- System remains stable if a feature fails

### 2. Observer Pattern (Event Bus)

Components observe events without direct coupling.

**Benefits:**
- Loose coupling
- Easy to add new subscribers
- Clear event flow

### 3. Dependency Injection

Features declare dependencies explicitly.

**Benefits:**
- Clear dependency graph
- Prevents circular dependencies
- Easy to test with mocks

### 4. Strategy Pattern (Error Recovery)

Different recovery strategies for different error types.

**Benefits:**
- Flexible error handling
- Easy to add new strategies
- Context-aware recovery

### 5. Singleton Pattern

Core managers are singletons (FeatureManager, EventBus, ConfigurationManager).

**Benefits:**
- Global access point
- Single source of truth
- Resource efficiency

### 6. Factory Pattern (Page Creation)

VulkanModPageFactory creates GUI pages dynamically.

**Benefits:**
- Consistent page structure
- Easy to add new pages
- Centralized creation logic

---

## Performance Considerations

### Optimization Strategies

#### 1. Lazy Initialization

```java
// ❌ Bad: Initialize everything at startup
public void init() {
    loadAllTextures();
    loadAllModels();
    loadAllSounds();
}

// ✅ Good: Initialize on first use
private TextureCache cache = null;

public TextureCache getCache() {
    if (cache == null) {
        cache = new TextureCache();
    }
    return cache;
}
```

#### 2. Caching

```java
// Cache translation Text objects
private static final Map<String, Text> TRANSLATION_CACHE = new HashMap<>();

public static Text getTranslation(String key) {
    return TRANSLATION_CACHE.computeIfAbsent(
        key,
        k -> Text.translatable(k)
    );
}
```

#### 3. Event Batching

```java
// Batch config changes
ConfigBatcher batcher = new ConfigBatcher();
batcher.queue("fog", false);
batcher.queue("particles", false);
batcher.queue("animations", false);
batcher.flush();  // Single save, single event
```

#### 4. Conditional Ticking

```java
// Only tick when needed
@Override
public void onTick(MinecraftClient minecraft) {
    // Skip if nothing to do
    if (!hasWork()) {
        return;
    }

    // Rate limit
    if (tickCounter++ % 20 != 0) {
        return;  // Only run every second
    }

    doWork();
}
```

### Performance Metrics

| Operation | Target Time | Notes |
|-----------|-------------|-------|
| Feature.onTick() | < 0.1ms | Called every frame |
| Config save | < 5ms | Async when possible |
| GUI page creation | < 50ms | One-time cost |
| Event dispatch | < 0.5ms | Per event |
| Dependency resolution | < 10ms | On startup only |

---

## Summary

VulkanMod Extra's architecture prioritizes:

1. **Modularity** - Features are independent plugins
2. **Reliability** - Graceful error handling and recovery
3. **Extensibility** - Easy to add new features
4. **Performance** - Minimal overhead, optimized hot paths
5. **Maintainability** - Clear separation of concerns
6. **Compatibility** - Multi-version support through abstraction

This design enables rapid development while maintaining stability and performance across multiple Minecraft versions.

---

<div align="center">

**Want to contribute?** Check out [CONTRIBUTING.md](CONTRIBUTING.md)

**Questions?** Open a [Discussion](https://github.com/CriticalRange/vulkanmod-extra/discussions)

*Architecture designed for performance and maintainability* 🏗️

</div>
