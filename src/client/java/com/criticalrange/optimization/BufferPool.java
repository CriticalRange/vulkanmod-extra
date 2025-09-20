package com.criticalrange.optimization;

import com.criticalrange.VulkanModExtra;
import net.vulkanmod.vulkan.memory.MemoryType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Buffer pooling system for VulkanMod optimization
 * Reduces allocation overhead by reusing GPU buffers
 */
public class BufferPool {

    // Singleton instance
    private static volatile BufferPool instance;
    private static volatile boolean isShuttingDown = false;

    // Static initializer to register shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(BufferPool::shutdown, "BufferPool-Shutdown"));
    }

    // Buffer type categories for pooling
    public enum BufferType {
        VERTEX(1),    // VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
        INDEX(2),     // VK_BUFFER_USAGE_INDEX_BUFFER_BIT
        UNIFORM(16),  // VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT
        STAGING(64);  // VK_BUFFER_USAGE_TRANSFER_SRC_BIT

        public final int usage;

        BufferType(int usage) {
            this.usage = usage;
        }

        public static BufferType fromUsage(int usage) {
            if ((usage & VERTEX.usage) != 0) return VERTEX;
            if ((usage & INDEX.usage) != 0) return INDEX;
            if ((usage & UNIFORM.usage) != 0) return UNIFORM;
            if ((usage & STAGING.usage) != 0) return STAGING;
            return VERTEX; // Default fallback
        }
    }

    // Size buckets for efficient buffer reuse (in bytes)
    private static final long[] SIZE_BUCKETS = {
        16 * 1024,      // 16KB
        64 * 1024,      // 64KB
        256 * 1024,     // 256KB
        1024 * 1024,    // 1MB
        4 * 1024 * 1024,    // 4MB
        16 * 1024 * 1024,   // 16MB
        64 * 1024 * 1024    // 64MB
    };

    // Pool storage: BufferType -> SizeBucket -> Queue of buffers
    private final ConcurrentHashMap<BufferType, ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PooledBuffer>>> pools;

    // Memory tracking
    private final AtomicLong totalPooledMemory = new AtomicLong(0);
    private final AtomicLong poolHits = new AtomicLong(0);
    private final AtomicLong poolMisses = new AtomicLong(0);
    private final AtomicLong buffersPooled = new AtomicLong(0);

    // Frame-based cleanup tracking
    private volatile long lastCleanupFrame = 0;
    private volatile long lastFullCleanupTime = 0;
    private static final int CLEANUP_INTERVAL_FRAMES = 60; // Every 60 frames (~1 second)
    private static final long FULL_CLEANUP_INTERVAL_MS = 30000; // Every 30 seconds

    // Configuration
    private volatile boolean enabled = true;
    private volatile long maxPoolMemoryBytes = 64L * 1024 * 1024; // 64MB default

    private BufferPool() {
        this.pools = new ConcurrentHashMap<>();
        for (BufferType type : BufferType.values()) {
            pools.put(type, new ConcurrentHashMap<>());
        }

        // Initialize cleanup timestamps
        this.lastCleanupFrame = 0;
        this.lastFullCleanupTime = System.currentTimeMillis();

        if (VulkanModExtra.LOGGER != null) {
            VulkanModExtra.LOGGER.info("BufferPool initialized - pooling enabled");
        }
    }

    /**
     * Get singleton instance with thread-safe lazy initialization
     */
    public static BufferPool getInstance() {
        if (instance == null) {
            synchronized (BufferPool.class) {
                if (instance == null) {
                    instance = new BufferPool();
                }
            }
        }
        return instance;
    }

    /**
     * Attempt to acquire a buffer from the pool
     * @param usage Buffer usage flags
     * @param memoryType Memory type for allocation
     * @param size Required buffer size in bytes
     * @return Pooled buffer if available, null if pool miss
     */
    public PooledBuffer acquireBuffer(int usage, MemoryType memoryType, long size) {
        if (!enabled || !isConfigEnabled()) {
            poolMisses.incrementAndGet();
            return null;
        }

        try {
            BufferType bufferType = BufferType.fromUsage(usage);
            int sizeBucket = getSizeBucket(size);

            ConcurrentLinkedQueue<PooledBuffer> bucketQueue = pools.get(bufferType).get(sizeBucket);
            if (bucketQueue == null) {
                poolMisses.incrementAndGet();
                return null;
            }

            PooledBuffer pooledBuffer = bucketQueue.poll();
            if (pooledBuffer != null && pooledBuffer.isValidForReuse(size, memoryType)) {
                // Reset buffer state for reuse
                pooledBuffer.resetForReuse();
                poolHits.incrementAndGet();
                totalPooledMemory.addAndGet(-pooledBuffer.getSize());

                if (VulkanModExtra.LOGGER != null) {
                    VulkanModExtra.LOGGER.debug("Buffer pool hit: {} type, {} bytes, bucket {}",
                        bufferType, size, sizeBucket);
                }

                return pooledBuffer;
            } else if (pooledBuffer != null) {
                // Buffer invalid, clean it up
                cleanupInvalidBuffer(pooledBuffer);
            }

            poolMisses.incrementAndGet();
            return null;

        } catch (Exception e) {
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.warn("Buffer pool acquire failed: {}", e.getMessage());
            }
            poolMisses.incrementAndGet();
            return null;
        }
    }

    /**
     * Return a buffer to the pool for reuse
     * @param buffer Buffer to pool
     * @param usage Buffer usage flags
     * @param size Buffer size in bytes
     * @return true if buffer was pooled, false if rejected
     */
    public boolean returnBuffer(Object buffer, int usage, long size) {
        if (!enabled || !isConfigEnabled() || buffer == null) {
            return false;
        }

        // Check memory pressure
        if (isMemoryPressureHigh()) {
            return false;
        }

        try {
            BufferType bufferType = BufferType.fromUsage(usage);
            int sizeBucket = getSizeBucket(size);

            // Get or create bucket queue
            ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PooledBuffer>> typeMap = pools.get(bufferType);
            ConcurrentLinkedQueue<PooledBuffer> bucketQueue = typeMap.computeIfAbsent(sizeBucket,
                k -> new ConcurrentLinkedQueue<>());

            // Create pooled buffer wrapper
            PooledBuffer pooledBuffer = new PooledBuffer(buffer, size, System.currentTimeMillis());

            // Add to pool
            bucketQueue.offer(pooledBuffer);
            buffersPooled.incrementAndGet();
            totalPooledMemory.addAndGet(size);

            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.debug("Buffer returned to pool: {} type, {} bytes, bucket {}",
                    bufferType, size, sizeBucket);
            }

            // Cleanup old buffers if pool is getting large
            cleanupOldBuffers();

            return true;

        } catch (Exception e) {
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.warn("Buffer pool return failed: {}", e.getMessage());
            }
            return false;
        }
    }

    /**
     * Get appropriate size bucket for a buffer size
     */
    private int getSizeBucket(long size) {
        for (int i = 0; i < SIZE_BUCKETS.length; i++) {
            if (size <= SIZE_BUCKETS[i]) {
                return i;
            }
        }
        return SIZE_BUCKETS.length - 1; // Largest bucket for oversized buffers
    }

    /**
     * Check if memory pressure is too high for pooling
     */
    private boolean isMemoryPressureHigh() {
        long currentPoolMemory = totalPooledMemory.get();
        return currentPoolMemory > maxPoolMemoryBytes;
    }

    /**
     * Check if buffer pooling is enabled in configuration
     */
    private boolean isConfigEnabled() {
        try {
            if (VulkanModExtra.CONFIG != null && VulkanModExtra.CONFIG.optimizationSettings != null) {
                // Update pool size limit from config
                long configSizeMB = VulkanModExtra.CONFIG.optimizationSettings.bufferPoolSize;
                long newMaxPoolMemoryBytes = configSizeMB * 1024 * 1024;

                // Log if pool size limit changed
                if (newMaxPoolMemoryBytes != maxPoolMemoryBytes) {
                    if (VulkanModExtra.LOGGER != null) {
                        VulkanModExtra.LOGGER.debug("Buffer pool size limit updated: {}MB -> {}MB",
                            maxPoolMemoryBytes / (1024 * 1024), configSizeMB);
                    }
                    maxPoolMemoryBytes = newMaxPoolMemoryBytes;
                }

                return VulkanModExtra.CONFIG.optimizationSettings.bufferPooling;
            }
        } catch (Exception e) {
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.warn("Failed to read buffer pool config: {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * Force enable buffer pooling for testing/debugging
     */
    public void forceEnable() {
        this.enabled = true;
        if (VulkanModExtra.LOGGER != null) {
            VulkanModExtra.LOGGER.info("Buffer pooling force-enabled");
        }
    }

    /**
     * Get current pool activation status for debugging
     */
    public boolean isActive() {
        return enabled && isConfigEnabled();
    }

    /**
     * Frame-based cleanup called every frame
     * Performs lightweight cleanup to prevent memory leaks
     */
    public void onFrameRender(long frameCount) {
        if (!enabled || !isConfigEnabled()) {
            return;
        }

        try {
            // Lightweight cleanup every frame
            performLightweightCleanup();

            // Periodic cleanup every N frames
            if (frameCount - lastCleanupFrame >= CLEANUP_INTERVAL_FRAMES) {
                performPeriodicCleanup();
                lastCleanupFrame = frameCount;
            }

            // Full cleanup every 30 seconds
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFullCleanupTime >= FULL_CLEANUP_INTERVAL_MS) {
                performFullCleanup();
                lastFullCleanupTime = currentTime;
            }

        } catch (Exception e) {
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.debug("Frame cleanup error: {}", e.getMessage());
            }
        }
    }

    /**
     * Lightweight cleanup performed every frame
     */
    private void performLightweightCleanup() {
        // Check memory pressure and reduce pool if needed
        if (isMemoryPressureHigh()) {
            reducePoolSize(0.25); // Reduce pool by 25% under pressure
        }
    }

    /**
     * Periodic cleanup performed every ~1 second
     */
    private void performPeriodicCleanup() {
        long currentTime = System.currentTimeMillis();
        long maxAge = 10000; // 10 seconds for periodic cleanup
        int cleanedBuffers = 0;

        // Clean aged buffers from all pools
        for (ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PooledBuffer>> typeMap : pools.values()) {
            for (ConcurrentLinkedQueue<PooledBuffer> queue : typeMap.values()) {
                cleanedBuffers += cleanQueue(queue, currentTime, maxAge);
            }
        }

        if (cleanedBuffers > 0 && VulkanModExtra.LOGGER != null) {
            VulkanModExtra.LOGGER.debug("Periodic cleanup: removed {} aged buffers", cleanedBuffers);
        }
    }

    /**
     * Full cleanup performed every 30 seconds
     */
    private void performFullCleanup() {
        long currentTime = System.currentTimeMillis();
        long maxAge = 30000; // 30 seconds for full cleanup
        int cleanedBuffers = 0;
        long memoryFreed = 0;

        // Aggressive cleanup of all aged buffers
        for (ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PooledBuffer>> typeMap : pools.values()) {
            for (ConcurrentLinkedQueue<PooledBuffer> queue : typeMap.values()) {
                int cleaned = cleanQueue(queue, currentTime, maxAge);
                cleanedBuffers += cleaned;
            }
        }

        // Compact empty buckets
        compactEmptyBuckets();

        if (VulkanModExtra.LOGGER != null) {
            PoolStatistics stats = getStatistics();
            VulkanModExtra.LOGGER.debug("Full cleanup: removed {} buffers, pool memory: {}MB, hit rate: {:.2f}%",
                cleanedBuffers,
                stats.totalMemoryBytes / (1024 * 1024),
                stats.getHitRate() * 100);
        }
    }

    /**
     * Clean buffers from a queue based on age
     */
    private int cleanQueue(ConcurrentLinkedQueue<PooledBuffer> queue, long currentTime, long maxAge) {
        int cleaned = 0;
        PooledBuffer buffer;

        // Use iterator to safely remove elements
        var iterator = queue.iterator();
        while (iterator.hasNext()) {
            buffer = iterator.next();
            if (currentTime - buffer.getTimestamp() > maxAge) {
                iterator.remove();
                totalPooledMemory.addAndGet(-buffer.getSize());
                cleanupInvalidBuffer(buffer);
                cleaned++;
            }
        }

        return cleaned;
    }

    /**
     * Reduce pool size under memory pressure
     */
    private void reducePoolSize(double reductionFactor) {
        int targetReduction = (int) (buffersPooled.get() * reductionFactor);
        int removed = 0;

        for (ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PooledBuffer>> typeMap : pools.values()) {
            for (ConcurrentLinkedQueue<PooledBuffer> queue : typeMap.values()) {
                while (removed < targetReduction && !queue.isEmpty()) {
                    PooledBuffer buffer = queue.poll();
                    if (buffer != null) {
                        totalPooledMemory.addAndGet(-buffer.getSize());
                        cleanupInvalidBuffer(buffer);
                        removed++;
                    }
                }
                if (removed >= targetReduction) break;
            }
            if (removed >= targetReduction) break;
        }

        if (removed > 0 && VulkanModExtra.LOGGER != null) {
            VulkanModExtra.LOGGER.debug("Memory pressure cleanup: removed {} buffers", removed);
        }
    }

    /**
     * Compact empty buckets to reduce memory overhead
     */
    private void compactEmptyBuckets() {
        for (ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PooledBuffer>> typeMap : pools.values()) {
            typeMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
    }

    /**
     * Legacy cleanup method for compatibility
     */
    private void cleanupOldBuffers() {
        // This method is now handled by frame-based cleanup
        // Keeping for backward compatibility
        performPeriodicCleanup();
    }

    /**
     * Cleanup an invalid buffer
     */
    private void cleanupInvalidBuffer(PooledBuffer pooledBuffer) {
        try {
            // Buffer cleanup will be handled by VulkanMod's normal cleanup process
            // We just need to remove it from our tracking
            totalPooledMemory.addAndGet(-pooledBuffer.getSize());
        } catch (Exception e) {
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.debug("Buffer cleanup warning: {}", e.getMessage());
            }
        }
    }

    /**
     * Get pool statistics for monitoring
     */
    public PoolStatistics getStatistics() {
        return new PoolStatistics(
            poolHits.get(),
            poolMisses.get(),
            buffersPooled.get(),
            totalPooledMemory.get(),
            maxPoolMemoryBytes,
            pools.values().stream()
                .mapToInt(typeMap -> typeMap.values().stream()
                    .mapToInt(ConcurrentLinkedQueue::size).sum())
                .sum()
        );
    }

    /**
     * Force cleanup of all pooled buffers
     */
    public void cleanup() {
        try {
            for (ConcurrentHashMap<Integer, ConcurrentLinkedQueue<PooledBuffer>> typeMap : pools.values()) {
                for (ConcurrentLinkedQueue<PooledBuffer> queue : typeMap.values()) {
                    PooledBuffer buffer;
                    while ((buffer = queue.poll()) != null) {
                        cleanupInvalidBuffer(buffer);
                    }
                }
                typeMap.clear();
            }

            totalPooledMemory.set(0);
            buffersPooled.set(0);

            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.info("Buffer pool cleanup completed");
            }

        } catch (Exception e) {
            if (VulkanModExtra.LOGGER != null) {
                VulkanModExtra.LOGGER.error("Buffer pool cleanup failed", e);
            }
        }
    }

    /**
     * Enable or disable buffer pooling
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            cleanup();
        }
    }

    /**
     * Pool statistics for monitoring
     */
    public static class PoolStatistics {
        public final long hits;
        public final long misses;
        public final long buffersPooled;
        public final long totalMemoryBytes;
        public final long maxMemoryBytes;
        public final int totalBuffersInPool;

        public PoolStatistics(long hits, long misses, long buffersPooled,
                            long totalMemoryBytes, long maxMemoryBytes, int totalBuffersInPool) {
            this.hits = hits;
            this.misses = misses;
            this.buffersPooled = buffersPooled;
            this.totalMemoryBytes = totalMemoryBytes;
            this.maxMemoryBytes = maxMemoryBytes;
            this.totalBuffersInPool = totalBuffersInPool;
        }

        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        }

        public double getMemoryUtilization() {
            return maxMemoryBytes > 0 ? (double) totalMemoryBytes / maxMemoryBytes : 0.0;
        }
    }

    /**
     * Shutdown method to clean up all buffers and prevent memory leaks
     */
    public static void shutdown() {
        isShuttingDown = true;
        try {
            BufferPool pool = instance;
            if (pool != null) {
                pool.cleanup();
                instance = null;
            }
            VulkanModExtra.LOGGER.info("BufferPool shutdown completed");
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Error during BufferPool shutdown", e);
        }
    }

    /**
     * Check if the buffer pool is shutting down
     * @return true if shutting down
     */
    public static boolean isShuttingDown() {
        return isShuttingDown;
    }
}