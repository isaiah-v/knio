package org.ivcode.knio.context

import java.nio.ByteBuffer

class ByteBufferPoolNone(
    private val isDirect: Boolean = false
): ByteBufferPool {

    override fun acquire(size: Int): ByteBuffer = if (isDirect) {
        ByteBuffer.allocateDirect(size)
    } else {
        ByteBuffer.allocate(size)
    }

    override fun release(buffer: ByteBuffer) {
        // do nothing
    }
}
