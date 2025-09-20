package com.criticalrange.optimization;

import net.vulkanmod.vulkan.memory.MemoryType;

/**
 * Wrapper for pooled buffer objects
 * Tracks buffer metadata for pool management
 */
public class PooledBuffer {

    private final Object buffer;
    private final long size;
    private final long timestamp;
    private boolean inUse;
    private MemoryType memoryType;

    public PooledBuffer(Object buffer, long size, long timestamp) {
        this.buffer = buffer;
        this.size = size;
        this.timestamp = timestamp;
        this.inUse = false;
    }

    /**
     * Get the underlying buffer object
     */
    public Object getBuffer() {
        return buffer;
    }

    /**
     * Get buffer size in bytes
     */
    public long getSize() {
        return size;
    }

    /**
     * Get timestamp when buffer was pooled
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Check if buffer is currently in use
     */
    public boolean isInUse() {
        return inUse;
    }

    /**
     * Check if buffer is valid for reuse with given parameters
     */
    public boolean isValidForReuse(long requiredSize, MemoryType requiredMemoryType) {
        // Buffer must be large enough and not in use
        if (inUse || size < requiredSize) {
            return false;
        }

        // Memory type compatibility check
        if (memoryType != null && !memoryType.equals(requiredMemoryType)) {
            return false;
        }

        // Buffer size should not be excessively larger (avoid memory waste)
        // Allow up to 2x the required size for reuse
        if (size > requiredSize * 2) {
            return false;
        }

        return true;
    }

    /**
     * Reset buffer state for reuse
     */
    public void resetForReuse() {
        this.inUse = true;
        // Note: Actual buffer content reset would be handled by VulkanMod
    }

    /**
     * Mark buffer as released from use
     */
    public void release() {
        this.inUse = false;
    }

    /**
     * Set memory type for compatibility checking
     */
    public void setMemoryType(MemoryType memoryType) {
        this.memoryType = memoryType;
    }

    /**
     * Get memory type
     */
    public MemoryType getMemoryType() {
        return memoryType;
    }

    @Override
    public String toString() {
        return String.format("PooledBuffer{size=%d, inUse=%s, age=%dms}",
            size, inUse, System.currentTimeMillis() - timestamp);
    }
}