# Contributing to VulkanMod Extra

[![GitHub Issues](https://img.shields.io/github/issues/CriticalRange/vulkanmod-extra)](https://github.com/CriticalRange/vulkanmod-extra/issues)
[![GitHub Pull Requests](https://img.shields.io/github/issues-pr/CriticalRange/vulkanmod-extra)](https://github.com/CriticalRange/vulkanmod-extra/pulls)
[![License](https://img.shields.io/github/license/CriticalRange/vulkanmod-extra?color=blue)](https://github.com/CriticalRange/vulkanmod-extra/blob/main/LICENSE)

**Thank you for your interest in contributing to VulkanMod Extra!** Whether you're fixing a bug, adding a feature, translating to a new language, or improving documentation, your contributions help make Minecraft run better for everyone.

---

## Table of Contents

- [Ways to Contribute](#ways-to-contribute)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Code Contributions](#code-contributions)
- [Translation Contributions](#translation-contributions)
- [Reporting Issues](#reporting-issues)
- [Code Style & Standards](#code-style--standards)
- [Pull Request Process](#pull-request-process)
- [Community Guidelines](#community-guidelines)

---

## Ways to Contribute

There are many ways to help improve VulkanMod Extra:

### 🐛 Bug Reports & Testing
- Report bugs with detailed reproduction steps
- Test new features and provide feedback
- Verify fixes for existing issues
- Test on different hardware configurations

### 💻 Code Contributions
- Fix bugs and implement new features
- Improve performance and optimize code
- Add new rendering controls and options
- Enhance the feature system architecture

### 🌍 Translations
- Translate the mod to your language
- Improve existing translations
- Review and verify community translations
- Update translations for new features

### 📚 Documentation
- Improve README and guides
- Write tutorials and examples
- Create diagrams and visuals
- Document internal architecture

### 💡 Feature Suggestions
- Propose new features and improvements
- Discuss design decisions
- Provide use cases and examples
- Help prioritize the roadmap

---

## Getting Started

### Prerequisites

Before you begin, make sure you have:

- **Java Development Kit (JDK) 21** or higher
- **Git** for version control
- **IntelliJ IDEA** (recommended) or your preferred Java IDE
- **Minecraft** 1.21.1+ with Fabric Loader installed
- Basic understanding of Java and Gradle

### Understanding the Project

Read these documents to understand the codebase:

1. **[README.md](README.md)** - Project overview and features
2. **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed architecture documentation
3. **[CLAUDE.md](CLAUDE.md)** - Development guidelines and build system
4. **[CHANGELOG.md](CHANGELOG.md)** - Recent changes and version history

---

## Development Setup

### 1. Fork and Clone

```bash
# Fork the repository on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/vulkanmod-extra.git
cd vulkanmod-extra

# Add upstream remote for syncing
git remote add upstream https://github.com/CriticalRange/vulkanmod-extra.git
```

### 2. Build the Project

```bash
# Make gradlew executable (Linux/Mac)
chmod +x ./gradlew

# Build all modules
./gradlew build

# Run Minecraft with the mod (for testing)
./gradlew :fabric-1.21.1:runClient
```

**Build Output**: Compiled JARs are in `fabric-{version}/build/libs/`

### 3. Import into IDE

**IntelliJ IDEA** (Recommended):
1. File → Open → Select the `vulkanmod-extra` folder
2. IntelliJ will auto-import the Gradle project
3. Wait for indexing to complete
4. Run configurations will be generated automatically

**Eclipse**:
1. Import → Gradle → Existing Gradle Project
2. Select the root directory
3. Complete the import wizard

### 4. Verify Setup

```bash
# Clean build to ensure everything works
./gradlew clean build

# Expected output: BUILD SUCCESSFUL
```

---

## Code Contributions

### Architecture Overview

VulkanMod Extra uses a modular architecture:

- **`common/`** - Shared code across all Minecraft versions
  - `core/` - Feature system, dependency management, event bus
  - `config/` - Configuration management and serialization
  - `integration/` - VulkanMod GUI integration
  - `mixins/` - Common Mixin modifications
  - `features/` - Individual feature implementations

- **`fabric-{version}/`** - Version-specific code
  - `mixins/` - Version-specific Mixin modifications
  - Overrides for changed APIs between versions

See **[ARCHITECTURE.md](ARCHITECTURE.md)** for detailed system design.

### Adding a New Feature

Features are the core of VulkanMod Extra. Here's how to add one:

#### 1. Create the Feature Class

```java
package com.criticalrange.features.myfeature;

import com.criticalrange.core.BaseFeature;
import com.criticalrange.core.FeatureCategory;
import net.minecraft.client.MinecraftClient;

/**
 * Controls [your feature description]
 */
public class MyFeature extends BaseFeature {

    public MyFeature() {
        super(
            "my_feature",              // Unique ID
            "My Feature",              // Display name
            "1.0.0",                   // Version
            FeatureCategory.RENDER,    // Category
            "YourName"                 // Author
        );
    }

    @Override
    public void initialize(MinecraftClient minecraft) {
        // Initialize resources, register listeners, etc.
        LOGGER.info("My Feature initialized!");
    }

    @Override
    public void onEnable() {
        // Called when feature is enabled
        LOGGER.info("My Feature enabled");
    }

    @Override
    public void onDisable() {
        // Called when feature is disabled - clean up resources
        LOGGER.info("My Feature disabled");
    }

    @Override
    public void onTick(MinecraftClient minecraft) {
        // Called every game tick when enabled
        // Keep this lightweight!
    }

    @Override
    public boolean performHealthCheck() {
        // Verify feature is working correctly
        return true;
    }
}
```

#### 2. Add Configuration Fields

Edit `VulkanModExtraConfig.java`:

```java
public static class RenderSettings {
    // ... existing fields ...

    public boolean myFeature = true;  // Default enabled
    public int myFeatureIntensity = 100;  // Example integer setting
}
```

#### 3. Add Translation Keys

Edit `common/src/main/resources/assets/vulkanmod-extra/lang/en_us.json`:

```json
{
  "vulkanmod-extra.option.render.myFeature": "My Feature",
  "vulkanmod-extra.option.render.myFeature.tooltip": "Enables my awesome feature\n\n§7Performance Impact: §eLow",
  "vulkanmod-extra.option.render.myFeatureIntensity": "My Feature Intensity",
  "vulkanmod-extra.option.render.myFeatureIntensity.tooltip": "Controls intensity (0-100)"
}
```

#### 4. Register in GUI

Edit `VulkanModPageFactory.java` to add your option to the appropriate page.

#### 5. Create Mixins (if needed)

```java
package com.criticalrange.mixins.myfeature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.callback.CallbackInfo;

@Mixin(TargetMinecraftClass.class)
public class MixinMyFeature {

    @Inject(method = "targetMethod", at = @At("HEAD"), cancellable = true)
    private void onTargetMethod(CallbackInfo ci) {
        if (!VulkanModExtra.CONFIG.renderSettings.myFeature) {
            ci.cancel();  // Skip rendering when disabled
        }
    }
}
```

#### 6. Register Mixin

Add to `common/src/main/resources/vulkanmod-extra.client.mixins.json`:

```json
{
  "mixins": [
    "myfeature.MixinMyFeature"
  ]
}
```

### Best Practices

✅ **DO:**
- Write clear, self-documenting code
- Add JavaDoc comments for public APIs
- Handle errors gracefully
- Use the ErrorRecoveryManager for critical operations
- Test on multiple Minecraft versions
- Keep performance impact minimal
- Use translation keys for all user-facing text

❌ **DON'T:**
- Hardcode strings (use `Text.translatable()`)
- Modify files outside your feature's scope
- Add dependencies without discussion
- Skip error handling
- Make breaking changes without warning
- Introduce memory leaks or performance regressions

---

## Translation Contributions

### Quick Start

1. Navigate to `common/src/main/resources/assets/vulkanmod-extra/lang/`
2. Copy `en_us.json` to your language code (e.g., `pt_pt.json` for Portuguese)
3. Translate all **values** (keep keys unchanged)
4. Submit a Pull Request

### Translation Guidelines

See the **[Translation README](common/src/main/resources/assets/vulkanmod-extra/lang/README.md)** for detailed instructions.

**Key Points:**
- Preserve formatting codes (`§7`, `§a`, `§e`, `§c`)
- Keep placeholders (`%s`, `\n`) intact
- Maintain the tone: informative but friendly
- Test in-game before submitting

**Supported Languages:**
We accept translations for any [official Minecraft language](https://minecraft.wiki/w/Language#Languages).

---

## Reporting Issues

### Before Creating an Issue

1. **Search existing issues** - Your issue may already be reported
2. **Check latest version** - Update and verify the bug still exists
3. **Verify it's VulkanMod Extra** - Test with only VulkanMod + VulkanMod Extra
4. **Gather information** - Have logs, screenshots, and system info ready

### Bug Report Template

Use our issue templates or include:

**Environment:**
- Minecraft Version: (e.g., 1.21.1)
- VulkanMod Version: (e.g., 0.5.5)
- VulkanMod Extra Version: (e.g., 0.2.0)
- Fabric Loader Version: (e.g., 0.17.2)
- Other Mods: (list all installed mods)
- Operating System: (Windows/Linux/macOS)
- GPU: (e.g., NVIDIA RTX 3060)

**Description:**
- What you expected to happen
- What actually happened
- Steps to reproduce
- Screenshots/videos (if applicable)
- Crash logs (upload to https://mclo.gs/)

### Feature Requests

Explain:
- The problem you're trying to solve
- Your proposed solution
- Alternative solutions considered
- Why this benefits the community
- Example use cases

---

## Code Style & Standards

### Java Style

We follow standard Java conventions with some specifics:

#### Naming Conventions

```java
// Classes: PascalCase
public class FeatureManager { }

// Methods and variables: camelCase
private boolean isFeatureEnabled;
public void enableFeature() { }

// Constants: UPPER_SNAKE_CASE
public static final String MOD_ID = "vulkanmod-extra";

// Packages: lowercase
package com.criticalrange.features;
```

#### Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: 120 characters max
- **Braces**: K&R style (opening brace on same line)
- **Imports**: Organize and remove unused

```java
// Good
if (condition) {
    doSomething();
} else {
    doSomethingElse();
}

// Bad
if (condition)
{
    doSomething();
}
else
{
    doSomethingElse();
}
```

#### Comments

```java
/**
 * JavaDoc for public classes and methods
 *
 * @param minecraft The Minecraft client instance
 * @return true if successful
 */
public boolean initialize(MinecraftClient minecraft) {
    // Inline comments for complex logic
    if (isComplexCondition()) {
        // Explain why, not what
        return handleComplexCase();
    }
    return true;
}
```

### Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples:**

```
feat(particles): add control for cherry blossom particles

Add new toggle for cherry blossom particle effects with performance
optimization for high-density particle areas.

Closes #123
```

```
fix(config): prevent beacon height from exceeding 512 blocks

Added validation to clamp beacon beam height between 32-512 blocks.
Invalid values in config.json will be auto-corrected on load.

Fixes #456
```

```
docs(i18n): add Japanese translation for particle controls

Contributed by @username
```

---

## Pull Request Process

### 1. Create a Feature Branch

```bash
# Update your fork
git fetch upstream
git checkout main
git merge upstream/main

# Create feature branch
git checkout -b feat/my-awesome-feature
```

### 2. Make Your Changes

- Write clean, tested code
- Follow the code style guidelines
- Add/update translations as needed
- Update documentation if applicable

### 3. Test Thoroughly

```bash
# Build and verify no errors
./gradlew build

# Run in-game
./gradlew :fabric-1.21.1:runClient

# Test your changes:
# - Does it work as expected?
# - Are there any visual glitches?
# - Does performance meet expectations?
# - Does it work with other features?
```

### 4. Commit Your Changes

```bash
# Stage your changes
git add .

# Commit with descriptive message
git commit -m "feat(particles): add cherry blossom particle control"

# Push to your fork
git push origin feat/my-awesome-feature
```

### 5. Open a Pull Request

1. Go to your fork on GitHub
2. Click "Compare & pull request"
3. Fill out the PR template:
   - **Title**: Clear, descriptive (matches commit message)
   - **Description**: What, why, and how
   - **Testing**: What you tested
   - **Screenshots**: If UI changes
   - **Related Issues**: Link with `Closes #123`

### 6. Code Review

- Address reviewer feedback promptly
- Make requested changes in new commits
- Re-request review when ready
- Be patient and professional

### 7. Merge

Once approved:
- Maintainers will squash and merge
- Your contribution will be in the next release!
- You'll be credited in CHANGELOG.md

---

## Community Guidelines

### Our Pledge

We are committed to providing a welcoming and inclusive environment for everyone, regardless of:

- Experience level
- Gender identity and expression
- Sexual orientation
- Disability
- Personal appearance
- Body size
- Race
- Ethnicity
- Age
- Religion
- Nationality

### Expected Behavior

✅ **Be Respectful**
- Use welcoming and inclusive language
- Respect differing viewpoints and experiences
- Accept constructive criticism gracefully
- Focus on what's best for the community

✅ **Be Collaborative**
- Help others learn and grow
- Share knowledge generously
- Credit others' work appropriately
- Communicate clearly and professionally

✅ **Be Patient**
- Remember that everyone was a beginner once
- Take time to explain concepts
- Provide constructive feedback
- Give others the benefit of the doubt

### Unacceptable Behavior

❌ **Never:**
- Harass, insult, or demean anyone
- Use sexualized language or imagery
- Share others' private information
- Make personal or political attacks
- Engage in trolling or inflammatory comments
- Spam or repeatedly post off-topic content

### Enforcement

Violations will result in:
1. **Warning** - First offense, friendly reminder
2. **Temporary Ban** - Repeated offenses
3. **Permanent Ban** - Severe or continued violations

Report violations to: [create issue with "Report" label]

---

## Recognition

### Contributors

All contributors are credited in:
- CHANGELOG.md for their contributions
- GitHub contributors page
- Special thanks in release notes

### Hall of Fame

Outstanding contributions may be recognized:
- 🏆 **Feature Architect** - Major feature implementations
- 🌍 **Translation Champion** - Complete language translations
- 🐛 **Bug Hunter** - Significant bug discoveries and fixes
- 📚 **Documentation Hero** - Extensive documentation improvements

---

## Getting Help

### Resources

- **Documentation**: [ARCHITECTURE.md](ARCHITECTURE.md), [CLAUDE.md](CLAUDE.md)
- **Discussions**: [GitHub Discussions](https://github.com/CriticalRange/vulkanmod-extra/discussions)
- **Issues**: [GitHub Issues](https://github.com/CriticalRange/vulkanmod-extra/issues)
- **Discord**: [Join VulkanMod Community]

### Common Questions

**Q: I'm new to modding. Where do I start?**
A: Check out [Fabric's documentation](https://fabricmc.net/wiki/tutorial:introduction) and our [ARCHITECTURE.md](ARCHITECTURE.md) to understand the basics.

**Q: How do I test my changes?**
A: Run `./gradlew :fabric-1.21.1:runClient` to launch Minecraft with your modified mod.

**Q: My build fails. What should I do?**
A: Ensure you're using JDK 21, run `./gradlew clean build`, and check the error messages.

**Q: Can I add a dependency?**
A: Discuss it in an issue first. We prefer to keep dependencies minimal.

**Q: How long until my PR is reviewed?**
A: Usually within a week. Be patient - maintainers are volunteers!

---

## Thank You! 💙

Every contribution, no matter how small, helps make Minecraft run better for thousands of players worldwide. Whether you're fixing a typo, translating a string, or implementing a major feature - **you're making a difference**.

**Welcome to the VulkanMod Extra community!** 🚀

---

<div align="center">

**Ready to contribute?**

[Report a Bug](https://github.com/CriticalRange/vulkanmod-extra/issues/new?template=bug_report.yml) • [Suggest a Feature](https://github.com/CriticalRange/vulkanmod-extra/issues/new?template=feature_request.yml) • [Start a Discussion](https://github.com/CriticalRange/vulkanmod-extra/discussions)

*Built with care for the Minecraft community* ❤️

</div>
