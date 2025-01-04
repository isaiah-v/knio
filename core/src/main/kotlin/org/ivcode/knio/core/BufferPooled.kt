package org.ivcode.knio.core

import java.nio.ByteBuffer

/**
 * A class that manages a pooled ByteBuffer with resizable and closeable capabilities.
 *
 * @property capacity The initial capacity of the buffer.
 * @property pool The ByteBufferPool used to acquire and release buffers.
 */
class BufferPooled (
    capacity: Int,
    private val pool: ByteBufferPool = ByteBufferPool.DEFAULT
) : BufferCloseable, BufferResizable, BufferAbstract() {

    // The current ByteBuffer instance managed by this class.
    private var buffer: ByteBuffer? = pool.acquire(capacity)

    /**
     * Returns the current ByteBuffer instance.
     *
     * @return The current ByteBuffer.
     * @throws IllegalStateException if the buffer has been closed.
     */
    override fun buffer(): ByteBuffer {
        return this.buffer ?: throw IllegalStateException("closed buffer")
    }

    /**
     * Resizes the current buffer to the specified capacity.
     *
     * The buffer is resized by acquiring a new buffer from the pool, copying the contents of the old buffer to the new
     * buffer, and releasing the old buffer back to the pool.
     *
     * @param capacity The new capacity for the buffer.
     */
    override fun resize(capacity: Int) {
        val old = buffer()
        val new = pool.acquire(capacity)
        new.put(old)

        buffer = new
        pool.release(old)
    }

    /**
     * Closes the buffer, releasing it back to the pool.
     */
    override fun close() {
        val buffer = this.buffer ?: return
        this.buffer = null

        pool.release(buffer)
    }
}