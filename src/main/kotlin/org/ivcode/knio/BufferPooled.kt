package org.ivcode.knio

import java.nio.ByteBuffer

class BufferPooled (
    capacity: Int,
    private val pool: ByteBufferPool = ByteBufferPool.DEFAULT
) : BufferCloseable, BufferResizable, BufferAbstract() {

    private var buffer: ByteBuffer? = pool.acquire(capacity)

    override fun buffer(): ByteBuffer {
        return this.buffer ?: throw IllegalStateException("closed buffer")
    }

    override fun resize(capacity: Int) {
        val old = buffer()
        val new = pool.acquire(capacity)
        new.put(old)

        buffer = new
        pool.release(old)
    }

    override fun close() {
        val buffer = this.buffer ?: return
        this.buffer = null

        pool.release(buffer)
    }
}