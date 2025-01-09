package org.ivcode.knio.system

import java.nio.ByteBuffer

interface ByteBufferPool {

    companion object {
        private var defaultPool: ByteBufferPool = ByteBufferNoPool()


        fun setDefault(pool: ByteBufferPool) {
            defaultPool = pool
        }

        fun getDefault(): ByteBufferPool {
            return defaultPool
        }
    }

    /**
     * Acquires a buffer of the specified size, allocating a new buffer if necessary. If a buffer exists equal to or
     * greater than the specified size, within the block size, that buffer will be returned.
     *
     * Acquired buffers are removed from the pool. To reuse a buffer, it must be released back to the pool.
     *
     * @param size The size of the buffer to acquire.
     * @return The buffer acquired.
     */
    fun acquire(size: Int): ByteBuffer

    /**
     * Releases the buffer back to the pool.
     *
     * @param buffer The buffer to release.
     */
    fun release(buffer: ByteBuffer)
}