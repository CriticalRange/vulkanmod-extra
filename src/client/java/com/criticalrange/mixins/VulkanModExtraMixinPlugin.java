package com.criticalrange.mixins;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Conditional mixin loading plugin for VulkanMod Extra
 * Handles version-specific mixin loading to avoid missing class issues
 * Simplified to avoid initialization issues
 */
public class VulkanModExtraMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        // Plugin loaded
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Simplified conditional loading - avoid accessing VersionHelper during mixin init

        // Handle VulkanMod-specific mixins
        if (mixinClassName.contains("optimization.MixinMemoryManager")) {
            return isClassAvailable("net.vulkanmod.vulkan.memory.buffer.Buffer");
        }

        // Skip problematic mixins that might cause issues
        if (mixinClassName.contains("MixinVulkanModEventTrigger")) {
            return isClassAvailable("net.vulkanmod.vulkan.VulkanMod");
        }

        // Load all other mixins - let runtime conditional logic handle version differences
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // Accept targets
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Pre-apply transformations
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Post-apply transformations
    }

    /**
     * Check if a class is available at runtime
     */
    private boolean isClassAvailable(String className) {
        try {
            Class.forName(className, false, this.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }
}