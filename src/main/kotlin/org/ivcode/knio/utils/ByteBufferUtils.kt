package org.ivcode.knio.utils

import org.ivcode.knio.context.ByteBufferPool
import java.nio.ByteBuffer

/**
 * Compacts the buffer if it has remaining bytes, otherwise increases the buffer size by the specified amount.
 *
 * @param increaseSize The amount to increase the buffer size by if it is full.
 * @param pool The pool to release the buffer to if it is increased in size.
 * @return The buffer compacted or increased in size.
 */
internal fun ByteBuffer.compactOrIncreaseSize(increaseSize: Int, pool: ByteBufferPool?=null): ByteBuffer {
    if (hasRemaining() || limit()!=capacity()) {
        compact()
    } else {
        val size = capacity() + increaseSize

        val newBuffer = pool?.acquire(size) ?:  ByteBuffer.allocate(size)
        newBuffer.put(this)
        pool?.release(this)
        return newBuffer
    }
    return this
}