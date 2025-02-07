package org.knio.core.context

import java.nio.ByteBuffer

/**
 * A non-implementation of [ByteBufferPool] that does not pool any buffers.
 *
 * This is the default implementation of [ByteBufferPool] for the knio library.
 */
class ByteBufferPoolNone (
    private val isDirect: Boolean = false
): ByteBufferPool {

    override fun acquire(size: Int): ByteBuffer = if (isDirect) {
        ByteBuffer.allocateDirect(size)
    } else {
        ByteBuffer.allocate(size)
    }

    override fun release(buffer: ByteBuffer) {
        // do nothing. allow the buffer to be garbage collected
    }
}
