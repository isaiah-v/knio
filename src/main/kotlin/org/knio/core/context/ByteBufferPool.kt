package org.knio.core.context

import java.nio.ByteBuffer

/**
 * A pool of byte buffers.
 *
 * Buffers allow for more flexible memory management, and can be reused to reduce memory allocation overhead. Note,
 * however, these pools are intended for the knio library. Once a buffer is released it is assumed to be no longer in
 * use, and should not be used elsewhere because it may be reused elsewhere in the library.
 */
interface ByteBufferPool {

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
     * While releasing a buffer is recommended, it is not required.
     * If a buffer is not released, it will be garbage collected
     * when it is no longer in use.
     *
     * @param buffer The buffer to release.
     */
    fun release(buffer: ByteBuffer)
}
