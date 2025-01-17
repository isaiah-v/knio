package org.ivcode.knio.context

import java.nio.*


private val DUMMY_BUFFER = ByteBuffer.wrap(byteArrayOf(0))

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
    fun resize(newSize: Int)
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
    private val pool: ByteBufferPool,
    size: Int,
    private val bytesPerUnit: Int,
    private val transformer: (ByteBuffer) -> T
) : ReleasableBuffer<T> {

    override var released = false
        private set

    private var buffer = pool.acquire(size * bytesPerUnit)

    override var value: T = transformer(buffer)
        get() {
            if (released) {
                throw IllegalStateException("Buffer has been released")
            }
            return field
        }
        private set



    /**
     * Releases the ByteBuffer back to the pool.
     */
    override fun release() {
        if (!released) {
            val buffer = buffer
            this.buffer = DUMMY_BUFFER
            this.value = transformer(DUMMY_BUFFER)

            pool.release(buffer)
            released = true
        }
    }

    /**
     * Resizes the buffer.
     *
     * This method will acquire a new buffer from the pool, copy the contents of the current buffer to the new buffer,
     * and release the current buffer back to the pool.
     *
     * @param newSize the new size of the buffer
     */
    override fun resize(newSize: Int) {
        if (released) {
            throw IllegalStateException("Buffer has been released")
        }

        val oldValue = value
        val valuePosition = oldValue.position()
        val valueLimit = oldValue.limit()
        val valueLimitDelta = valueLimit - valuePosition

        buffer.position(valuePosition * bytesPerUnit)
        buffer.limit(valueLimit * bytesPerUnit)

        val newBuffer = pool.acquire(newSize * bytesPerUnit)
        newBuffer.put(buffer) // TODO: check if this is correct
        newBuffer.position(0)
        newBuffer.limit(newBuffer.capacity())

        val newValue = transformer(newBuffer)
        newValue.limit(valueLimitDelta)

        buffer = newBuffer
        value = newValue

        pool.release(buffer)
    }
}

private fun asByteBuffer(buffer: ByteBuffer): ByteBuffer = buffer


/**
 * Transforms a ByteBuffer into a CharBuffer.
 *
 * @param buffer the ByteBuffer to transform
 * @return the resulting CharBuffer
 */
private fun asCharBuffer(buffer: ByteBuffer): CharBuffer = buffer.asCharBuffer()



/**
 * Acquires a releasable ByteBuffer from the pool.
 *
 * @param size the size of the ByteBuffer to acquire
 * @return a ReleasableBuffer containing the acquired ByteBuffer
 */
fun ByteBufferPool.acquireReleasableByteBuffer(size: Int): ReleasableBuffer<ByteBuffer> =
    ReleasableBufferImpl(this, size, 1, ::asByteBuffer)


/**
 * Acquires a releasable CharBuffer from the pool.
 *
 * @param size the size, in chars, of the CharBuffer to acquire.
 * @return a ReleasableBuffer containing the acquired CharBuffer
 */
fun ByteBufferPool.acquireReleasableCharBuffer(size: Int): ReleasableBuffer<CharBuffer> =
    ReleasableBufferImpl(this, size, BYTES_PER_CHAR, ::asCharBuffer)
