package org.ivcode.org.ivcode.knio.io.core

/**
 * An interface that represents a resizable buffer.
 * Extends the Buffer interface.
 */
interface BufferResizable: Buffer {
    /**
     * Resizes the buffer to the specified capacity.
     *
     * @param capacity The new capacity for the buffer.
     */
    fun resize(capacity: Int)
}