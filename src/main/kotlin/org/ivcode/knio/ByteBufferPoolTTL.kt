package org.ivcode.knio

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class ByteBufferPoolTTL(
    override val blockSize: Int = 1024,
    private val factory: (size: Int) -> ByteBuffer = ByteBuffer::allocateDirect,
    private val ttlMillis: Long = TimeUnit.MINUTES.toMillis(5), // Default TTL of 5 minutes
    private val cleanupIntervalMillis: Long = TimeUnit.SECONDS.toMillis(30) // Clean up every 30 seconds
) : ByteBufferPool {

    // Map of block sizes to their pools, each pool also tracks the last access time
    private val bufferPools = ConcurrentHashMap<Int, QueueWithTimestamp>()
    private var lastCleanupTime: Long = System.currentTimeMillis()

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

    override fun release(buffer: ByteBuffer) {
        performCleanupIfNeeded()

        val poolSize = alignSize(buffer.capacity())
        val poolWrapper = bufferPools.computeIfAbsent(poolSize) { QueueWithTimestamp() }

        poolWrapper.queue.add(buffer)
        poolWrapper.lastAccessedTime = System.currentTimeMillis() // Update access time when releasing
    }

    // Helper method to align size to blockSize
    private fun alignSize(size: Int): Int = ((size + blockSize - 1) / blockSize) * blockSize

    // Perform cleanup if enough time has passed since the last cleanup
    private fun performCleanupIfNeeded() {
        val currentTime = System.currentTimeMillis()

        if(atomicIsCleanup(currentTime)) {
            cleanUpExpiredPools(currentTime)
        }
    }

    // Clean up expired pools based on TTL
    private fun cleanUpExpiredPools(currentTime: Long) {
        bufferPools.entries.removeIf { (_, wrapper) -> currentTime - wrapper.lastAccessedTime > ttlMillis }
    }

    @Synchronized
    private fun atomicIsCleanup(currentTime: Long): Boolean =
        if (currentTime - lastCleanupTime > cleanupIntervalMillis) {
            lastCleanupTime = currentTime
            true
        } else {
            false
        }

    // Wrapper class to hold the queue and its last access time
    private class QueueWithTimestamp {
        val queue = ConcurrentLinkedQueue<ByteBuffer>()
        var lastAccessedTime: Long = System.currentTimeMillis()
    }
}
