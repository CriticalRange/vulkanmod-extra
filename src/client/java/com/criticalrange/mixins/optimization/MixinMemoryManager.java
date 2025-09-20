package com.criticalrange.mixins.optimization;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.optimization.BufferPool;
import com.criticalrange.optimization.PooledBuffer;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept VulkanMod's MemoryManager for buffer pooling optimization
 * Hooks into buffer creation and destruction to implement buffer reuse
 */
@Mixin(value = MemoryManager.class, remap = false)
public class MixinMemoryManager {

    @Unique
    private static volatile BufferPool vulkanmodExtra$bufferPool;

    /**
     * Get or initialize buffer pool instance
     */
    @Unique
    private static BufferPool vulkanmodExtra$getBufferPool() {
        if (vulkanmodExtra$bufferPool == null) {
            synchronized (MixinMemoryManager.class) {
                if (vulkanmodExtra$bufferPool == null) {
                    vulkanmodExtra$bufferPool = BufferPool.getInstance();
                }
            }
        }
        return vulkanmodExtra$bufferPool;
    }

    /**
     * Intercept buffer creation to check pool first
     * This targets the main buffer creation method in MemoryManager
     */
    @Inject(
        method = "createBuffer",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void vulkanmodExtra$interceptBufferCreation(
        long size,
        int usage,
        MemoryType memoryType,
        CallbackInfoReturnable<Buffer> cir
    ) {
        try {
            // Check if buffer pooling is enabled
            if (!vulkanmodExtra$isBufferPoolingEnabled()) {
                return;
            }

            BufferPool pool = vulkanmodExtra$getBufferPool();
            if (pool == null) {
                return;
            }

            // Try to get buffer from pool
            PooledBuffer pooledBuffer = pool.acquireBuffer(usage, memoryType, size);
            if (pooledBuffer != null && pooledBuffer.getBuffer() instanceof Buffer) {
                Buffer buffer = (Buffer) pooledBuffer.getBuffer();

                // Validate buffer is still valid
                if (vulkanmodExtra$isBufferValid(buffer, size)) {
                    if (VulkanModExtra.LOGGER != null) {
                        VulkanModExtra.LOGGER.debug("Buffer pool hit: reusing buffer of size {} bytes", size);
                    }

                    // Return pooled buffer, cancelling normal allocation
                    cir.setReturnValue(buffer);
                    return;
                }
            }

        } catch (Exception e) {
            // Log error but don't break normal allocation flow
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.warn("Buffer pool acquisition failed: {}", e.getMessage());
            }
        }

        // Continue with normal allocation if pool miss or error
    }

    /**
     * Intercept buffer freeing to return to pool instead
     * This targets the buffer cleanup methods
     */
    @Inject(
        method = "freeBuffer",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void vulkanmodExtra$interceptBufferFreeing(
        Buffer buffer,
        CallbackInfo ci
    ) {
        try {
            // Check if buffer pooling is enabled
            if (!vulkanmodExtra$isBufferPoolingEnabled()) {
                return;
            }

            BufferPool pool = vulkanmodExtra$getBufferPool();
            if (pool == null || buffer == null) {
                return;
            }

            // Get buffer metadata for pooling
            long bufferSize = vulkanmodExtra$getBufferSize(buffer);
            int bufferUsage = vulkanmodExtra$getBufferUsage(buffer);

            if (bufferSize > 0 && vulkanmodExtra$isBufferPoolable(buffer, bufferSize)) {
                // Try to return buffer to pool
                boolean pooled = pool.returnBuffer(buffer, bufferUsage, bufferSize);

                if (pooled) {
                    if (VulkanModExtra.LOGGER != null) {
                        VulkanModExtra.LOGGER.debug("Buffer returned to pool: {} bytes", bufferSize);
                    }

                    // Cancel normal buffer freeing since it's now pooled
                    ci.cancel();
                    return;
                }
            }

        } catch (Exception e) {
            // Log error but don't break normal cleanup flow
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.warn("Buffer pool return failed: {}", e.getMessage());
            }
        }

        // Continue with normal buffer freeing if pooling failed
    }

    /**
     * Hook into MemoryManager cleanup to clear buffer pool
     */
    @Inject(
        method = "cleanup",
        at = @At("HEAD"),
        remap = false
    )
    private static void vulkanmodExtra$cleanupBufferPool(CallbackInfo ci) {
        try {
            BufferPool pool = vulkanmodExtra$getBufferPool();
            if (pool != null) {
                pool.cleanup();

                if (VulkanModExtra.LOGGER != null) {
                    VulkanModExtra.LOGGER.info("Buffer pool cleaned up during MemoryManager cleanup");
                }
            }
        } catch (Exception e) {
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.error("Buffer pool cleanup failed", e);
            }
        }
    }

    /**
     * Check if buffer pooling is enabled in configuration
     */
    @Unique
    private static boolean vulkanmodExtra$isBufferPoolingEnabled() {
        try {
            return VulkanModExtra.CONFIG != null
                && VulkanModExtra.CONFIG.optimizationSettings != null
                && VulkanModExtra.CONFIG.optimizationSettings.bufferPooling;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate that a buffer is still usable using VulkanMod's Buffer API
     */
    @Unique
    private static boolean vulkanmodExtra$isBufferValid(Buffer buffer, long requiredSize) {
        try {
            if (buffer == null) {
                return false;
            }

            // Check if buffer is properly allocated (VulkanMod pattern)
            if (buffer.getId() == 0 || buffer.getAllocation() == 0) {
                return false;
            }

            // Check if buffer has sufficient size
            long bufferSize = buffer.getBufferSize();
            if (bufferSize < requiredSize) {
                return false;
            }

            return true;

        } catch (Exception e) {
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.debug("Buffer validation failed: {}", e.getMessage());
            }
            return false;
        }
    }

    /**
     * Check if a buffer should be pooled
     */
    @Unique
    private static boolean vulkanmodExtra$isBufferPoolable(Buffer buffer, long size) {
        try {
            // Don't pool very small buffers (< 16KB) - not worth the overhead
            if (size < 16 * 1024) {
                return false;
            }

            // Don't pool very large buffers (> 64MB) - too much memory pressure
            if (size > 64 * 1024 * 1024) {
                return false;
            }

            // Additional checks could be added here
            // e.g., buffer type restrictions, usage pattern analysis

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get buffer size using VulkanMod's Buffer API
     */
    @Unique
    private static long vulkanmodExtra$getBufferSize(Buffer buffer) {
        try {
            if (buffer != null) {
                // Direct access to buffer size via public method
                return buffer.getBufferSize();
            }
            return 0;

        } catch (Exception e) {
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.debug("Failed to get buffer size: {}", e.getMessage());
            }
            return 0;
        }
    }

    /**
     * Get buffer usage flags using VulkanMod's Buffer API
     */
    @Unique
    private static int vulkanmodExtra$getBufferUsage(Buffer buffer) {
        try {
            if (buffer != null) {
                // Direct access to usage flags via public field
                return buffer.usage;
            }
            return 1; // Default to vertex buffer usage

        } catch (Exception e) {
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.debug("Failed to get buffer usage: {}", e.getMessage());
            }
            return 1; // Default fallback
        }
    }
}