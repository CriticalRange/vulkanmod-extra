# VulkanMod Extra - Version Management System

This document describes the comprehensive version management system implemented for VulkanMod Extra to support multiple Minecraft versions from a single codebase.

## Overview

VulkanMod Extra uses a shared codebase with version-specific overrides to maintain compatibility across Minecraft 1.21.1, 1.21.2, 1.21.3, and 1.21.4. This approach minimizes code duplication while allowing precise version-specific modifications when needed.

## Supported Versions

- **Minecraft 1.21.1** - Stable baseline version
- **Minecraft 1.21.2** - Minor updates and fixes
- **Minecraft 1.21.3** - Additional compatibility updates
- **Minecraft 1.21.4** - Latest supported version

## Architecture

### Shared + Override System

The project uses a hierarchical file structure where version-specific overrides take precedence over shared code:

```
src/
├── main/                    # Shared code (loaded for all versions)
│   ├── java/
│   │   └── com/criticalrange/
│   │       ├── util/        # VersionHelper, MappingHelper, DevEnvironmentManager
│   │       ├── mixin/       # Common mixin configurations
│   │       └── features/    # Shared feature implementations
│   └── resources/
│       ├── vulkanmod-extra.mixins.json
│       └── fabric.mod.json
└── overrides/               # Version-specific overrides
    ├── v1_21_1/
    │   ├── java/           # 1.21.1 specific classes and mixins
    │   └── resources/
    ├── v1_21_2/
    ├── v1_21_3/
    └── v1_21_4/
```

### Override Priority

Version-specific files in `overrides/v{version}/` take precedence over shared files in `main/`. If a file with the same name and path exists in both locations, the version-specific version is used.

## Core Components

### 1. VersionHelper.java

Runtime version detection and utility class providing:
- Boolean flags for quick version checking (`IS_1_21_1`, `IS_1_21_2`, etc.)
- Version comparison methods (`isAtLeast()`, `isLessThan()`, `isBetween()`)
- Override directory detection (`hasOverrideDirectory()`)
- User-friendly version names (`getFriendlyName()`)

**Usage:**
```java
if (VersionHelper.IS_1_21_2) {
    // Version 1.21.2 specific code
}

if (VersionHelper.isAtLeast("1.21.3")) {
    // Code for 1.21.3 and later
}

if (VersionHelper.hasOverrideDirectory()) {
    // Override files are available for current version
}
```

### 2. MappingHelper.java

Handles version-specific method and field mappings using reflection:
- Cached reflection lookups for performance
- Version-specific method name aliases
- Fallback mechanisms for missing methods/fields
- Debug logging and cache statistics

**Usage:**
```java
Method method = MappingHelper.getMethod(targetClass, "methodName", parameterTypes);
Object result = MappingHelper.invokeMethod(target, methodName, paramTypes, params);
```

### 3. VulkanModExtraMixinPlugin.java

Mixin plugin that manages override system:
- Loads base mixins from `main/`
- Loads version-specific mixins from `overrides/v{version}/`
- Override mixins replace shared ones with same name
- Debug logging for loaded mixins

### 4. DevEnvironmentManager.java

Development utilities and in-game debugging:
- Version information display
- Debug mode with detailed logging
- Command handling for `/vulkanmod-extra reload`
- In-game version info on F3+V

## Version Management Tools

### 1. Version Switching

#### Command Line Usage
```bash
# Switch to specific version
./scripts/switch-version.sh 1.21.3
./scripts/switch-version.bat 1.21.2

# Quick gradle tasks
./gradlew switchTo1_21_3
./gradlew runClient1_21_3
```

#### What the Switch Script Does:
1. Updates `gradle.properties` with new Minecraft version
2. Loads version-specific dependencies from `gradle/versions/`
3. Regenerates project sources
4. Updates `.current-version` file
5. Shows active override directories

### 2. Override Creation

#### Manual Creation
```bash
# Create override for specific class/version
./scripts/create-override.sh MixinLevelRenderer 1.21.3
./scripts/create-override.bat "net.vulkanmod.extra.config.Config" 1.21.3
```

#### Override Directory Structure
```
src/overrides/v1_21_3/
├── java/
│   └── com/criticalrange/mixin/
│       └── MyMixin.java     # Overrides main/ version
└── resources/
    └── vulkanmod-extra-1.21.3.mixins.json  # Optional mixin config
```

### 3. Multi-Version Testing

```bash
# Test all supported versions
./scripts/test-all-versions.sh

# Generates compatibility report:
# test-results/compatibility-report.md
```

## Development Workflow

### 1. Basic Development
```bash
# Switch to target version
./scripts/switch-version.sh 1.21.3

# Create override if needed
./scripts/create-override.sh BrokenMixin 1.21.3

# Edit the override file
# src/overrides/v1_21_3/java/com/criticalrange/mixin/BrokenMixin.java

# Test and build
./gradlew runClient
./gradlew build
```

### 2. Testing All Versions
```bash
# Run comprehensive tests
./scripts/test-all-versions.sh

# Check compatibility report
cat test-results/compatibility-report.md

# Test specific version
./gradlew switchTo1_21_2 runClient
```

### 3. Debugging
```bash
# Enable debug mode
./gradlew -Dvulkanmod.mixin.debug=true runClient

# Use in-game commands
/vulkanmod-extra reload    # Reload configuration
/vulkanmod-extra debug     # Toggle debug mode
/vulkanmod-extra version   # Show version info
```

## Configuration Files

### Version Profiles (`gradle/versions/`)

Each version has a dedicated properties file:
```
gradle/versions/
├── 1.21.1.properties
├── 1.21.2.properties
├── 1.21.3.properties
└── 1.21.4.properties
```

Example (`1.21.3.properties`):
```properties
minecraft_version=1.21.3
yarn_mappings=1.21.3+build.1
fabric_version=0.116.5+1.21.3
fabric_api_version=0.116.5+1.21.3
vulkanmod_version=0.5.5
```

### Build Configuration

The `build.gradle` automatically:
- Loads version-specific dependencies
- Includes override directories in compilation
- Provides convenience tasks for version switching
- Builds release JARs for all versions

## Known Version-Specific Issues

### 1.21.1
- Baseline version, generally stable
- Minimal version-specific changes needed

### 1.21.2
- Some method signatures changed
- `renderLevel` vs `renderWorld` naming differences
- May require mixin overrides

### 1.21.3
- Additional method signature changes
- Field name updates (`level` -> `world`)
- Potential class relocations

### 1.21.4
- Latest version, most likely to have breaking changes
- Requires careful testing and validation
- Recommended as development baseline for future versions

## Best Practices

1. **Start with Shared Code**: Implement features in `main/` first
2. **Use VersionHelper**: Don't hardcode version checks
3. **Create Overrides Sparingly**: Only override when necessary
4. **Test Each Version**: Use `test-all-versions.sh` regularly
5. **Document Overrides**: Comment version-specific code clearly
6. **Maintain Version Profiles**: Keep gradle/versions/ files updated

## Troubleshooting

### Common Issues

1. **Mixin Not Loading**: Check override directory structure and plugin configuration
2. **Version Mismatch**: Run switch script to ensure correct version is set
3. **Compilation Errors**: Verify version profiles are correct and dependencies match
4. **Missing Overrides**: Create override files for version-specific changes

### Debug Commands

```bash
# Clean and regenerate
./gradlew clean genSources

# Debug logging
./gradlew -Dvulkanmod.mixin.debug=true runClient

# Check cache stats
./gradlew runClient --info | grep "MappingHelper"
```

## Future Considerations

- Add support for additional Minecraft versions as they become available
- Implement automated version detection and configuration
- Add more sophisticated mapping tools
- Enhance testing framework with automated validation

---

*This version management system enables efficient multi-version development while maintaining code quality and compatibility across supported Minecraft versions.*