package org.ivcode.knio.system

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * A ByteBuffer pool with time-to-live (TTL) and periodic cleanup capabilities.
 *
 * @property blockSize The size of each block in the pool.
 * @property factory A function to create new ByteBuffers.
 * @property ttlMillis The time-to-live for each buffer in milliseconds.
 * @property cleanupIntervalMillis The interval for cleaning up expired buffers in milliseconds.
 */
class ByteBufferPoolTTL(
    override val blockSize: Int = 1024,
    private val factory: (size: Int) -> ByteBuffer = ByteBuffer::allocateDirect,
    private val ttlMillis: Long = TimeUnit.MINUTES.toMillis(5), // Default TTL of 5 minutes
    private val cleanupIntervalMillis: Long = TimeUnit.SECONDS.toMillis(30) // Clean up every 30 seconds
) : ByteBufferPool {

    // Map of block sizes to their pools, each pool also tracks the last access time
    private val bufferPools = ConcurrentHashMap<Int, QueueWithTimestamp>()
    private var lastCleanupTime: Long = System.currentTimeMillis()

    /**
     * Acquires a ByteBuffer of the specified size from the pool.
     *
     * @param size The size of the ByteBuffer to acquire.
     * @return A ByteBuffer of the specified size.
     */
    override fun acquire(size: Int): ByteBuffer {
        val poolSize = alignSize(size)
        val poolWrapper = bufferPools.computeIfAbsent(poolSize) { QueueWithTimestamp() }

        // Update the access time when the pool is accessed
        poolWrapper.lastAccessedTime = System.currentTimeMillis()

        performCleanupIfNeeded()

        // Try to acquire a ByteBuffer from the pool
        while (true) {
            val buffer = poolWrapper.queue.poll() ?: break
            buffer.clear() // Reset buffer state before returning
            return buffer
        }

        // No available buffer, create a new one
        return factory(poolSize)
    }

    /**
     * Releases a ByteBuffer back to the pool.
     *
     * @param buffer The ByteBuffer to release.
     */
    override fun release(buffer: ByteBuffer) {
        performCleanupIfNeeded()

        val poolSize = alignSize(buffer.capacity())
        val poolWrapper = bufferPools.computeIfAbsent(poolSize) { QueueWithTimestamp() }

        poolWrapper.queue.add(buffer)
        poolWrapper.lastAccessedTime = System.currentTimeMillis() // Update access time when releasing
    }

    /**
     * Aligns the size to the block size.
     *
     * @param size The size to align.
     * @return The aligned size.
     */
    private fun alignSize(size: Int): Int = ((size + blockSize - 1) / blockSize) * blockSize

    /**
     * Performs cleanup if enough time has passed since the last cleanup.
     */
    private fun performCleanupIfNeeded() {
        val currentTime = System.currentTimeMillis()

        if(atomicIsCleanup(currentTime)) {
            cleanUpExpiredPools(currentTime)
        }
    }

    /**
     * Cleans up expired pools based on TTL.
     *
     * @param currentTime The current time in milliseconds.
     */
    private fun cleanUpExpiredPools(currentTime: Long) {
        bufferPools.entries.removeIf { (_, wrapper) -> currentTime - wrapper.lastAccessedTime > ttlMillis }
    }

    /**
     * Checks if cleanup is needed and updates the last cleanup time atomically.
     *
     * @param currentTime The current time in milliseconds.
     * @return True if cleanup is needed, false otherwise.
     */
    @Synchronized
    private fun atomicIsCleanup(currentTime: Long): Boolean =
        if (currentTime - lastCleanupTime > cleanupIntervalMillis) {
            lastCleanupTime = currentTime
            true
        } else {
            false
        }

    /**
     * A wrapper class to hold the queue and its last access time.
     */
    private class QueueWithTimestamp {
        val queue = ConcurrentLinkedQueue<ByteBuffer>()
        var lastAccessedTime: Long = System.currentTimeMillis()
    }
}