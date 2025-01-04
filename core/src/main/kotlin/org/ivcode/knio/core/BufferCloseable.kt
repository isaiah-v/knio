package org.ivcode.knio.core

/**
 * An interface that represents a closeable buffer.
 * Extends the Buffer and AutoCloseable interfaces.
 */
interface BufferCloseable: Buffer, AutoCloseable {
    /**
     * Closes the buffer, releasing any resources associated with it.
     */
    override fun close()
}