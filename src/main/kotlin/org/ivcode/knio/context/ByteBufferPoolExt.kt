package org.ivcode.knio.context

import java.nio.*

private const val BYTES_PER_CHAR  = 2
private const val BYTES_PER_SHORT = 2
private const val BYTES_PER_INT   = 4
private const val BYTES_PER_LONG  = 8

/**
 * Interface representing a buffer that can be released back to a pool.
 *
 * @param T the type of buffer
 */
interface ReleasableBuffer<T: Buffer> {
    val released: Boolean
    val value: T
    fun release()
}

/**
 * Implementation of ReleasableBuffer for ByteBuffer.
 *
 * @property value the ByteBuffer instance
 * @property pool the ByteBufferPool to release the buffer to
 */
internal class ReleasableByteBuffer (
    value: ByteBuffer,
    private val pool: ByteBufferPool
) : ReleasableBuffer<ByteBuffer> {

    override var released: Boolean = false
        private set

    override val value: ByteBuffer = value
        get() {
            if (released) {
                throw IllegalStateException("Buffer has been released")
            }
            return field
        }

    /**
     * Releases the ByteBuffer back to the pool.
     */
    override fun release() {
        released = true
        pool.release(value)
    }
}

/**
 * Generic implementation of ReleasableBuffer.
 *
 * @param T the type of buffer
 * @property buffer the ByteBuffer instance
 * @property pool the ByteBufferPool to release the buffer to
 * @property transformer a function to transform the ByteBuffer to the desired buffer type
 */
internal class ReleasableBufferImpl<T: Buffer>(
    private val buffer: ByteBuffer,
    private val pool: ByteBufferPool,
    transformer: (ByteBuffer) -> T
) : ReleasableBuffer<T> {

    override var released = false
        private set

    override val value: T = transformer(buffer)
        get() {
            if (released) {
                throw IllegalStateException("Buffer has been released")
            }
            return field
        }

    /**
     * Releases the ByteBuffer back to the pool.
     */
    override fun release() {
        released = true
        pool.release(buffer)
    }
}

/**
 * Acquires a releasable ByteBuffer from the pool.
 *
 * @param size the size of the ByteBuffer to acquire
 * @return a ReleasableBuffer containing the acquired ByteBuffer
 */
fun ByteBufferPool.acquireReleasableByteBuffer(size: Int): ReleasableBuffer<ByteBuffer> =
    ReleasableByteBuffer(acquire(size), this)


/**
 * Transforms a ByteBuffer into a CharBuffer.
 *
 * @param buffer the ByteBuffer to transform
 * @return the resulting CharBuffer
 */
private fun asCharBuffer(buffer: ByteBuffer): CharBuffer = buffer.asCharBuffer()

/**
 * Acquires a releasable CharBuffer from the pool.
 *
 * @param size the size, in chars, of the CharBuffer to acquire.
 * @return a ReleasableBuffer containing the acquired CharBuffer
 */
fun ByteBufferPool.acquireReleasableCharBuffer(size: Int): ReleasableBuffer<CharBuffer> =
    ReleasableBufferImpl(acquire(size * BYTES_PER_CHAR), this, ::asCharBuffer)

private fun asShortBuffer(buffer: ByteBuffer): ShortBuffer = buffer.asShortBuffer()

fun ByteBufferPool.acquireReleasableShortBuffer(size: Int): ReleasableBuffer<ShortBuffer> =
    ReleasableBufferImpl(acquire(size * BYTES_PER_SHORT), this, ::asShortBuffer)

private fun asIntBuffer(buffer: ByteBuffer): IntBuffer = buffer.asIntBuffer()

fun ByteBufferPool.acquireReleasableIntBuffer(size: Int): ReleasableBuffer<IntBuffer> =
    ReleasableBufferImpl(acquire(size * BYTES_PER_INT), this, ::asIntBuffer)

private fun asLongBuffer(buffer: ByteBuffer): LongBuffer = buffer.asLongBuffer()

fun ByteBufferPool.acquireReleasableLongBuffer(size: Int): ReleasableBuffer<LongBuffer> =
    ReleasableBufferImpl(acquire(size * BYTES_PER_LONG), this, ::asLongBuffer)