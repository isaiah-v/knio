package org.ivcode.knio

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.nio.CharBuffer
import kotlin.jvm.Throws
import kotlin.math.min

/**
 * Abstract class for reading character streams. The only methods that a subclass must implement are
 * read(char[], int, int) and close(). Most subclasses, however, will override some of the methods defined here in order
 * to provide higher efficiency, additional functionality, or both.
 *
 * The coroutine equivalent to the [java.io.Reader] class.
 */
abstract class KReader(
    protected val lock: Mutex = Mutex()
): KAutoCloseable {
    private var skipBuffer: CharArray? = null

    companion object {
        private const val MAX_SKIP_BUFFER_SIZE: Long = 8192L
    }

    /**
     * Marks the present position in the stream. Subsequent calls to reset() will attempt to reposition the stream to
     * this point. Not all character-input streams support the mark() operation.
     *
     * @param readLimit the maximum limit of bytes that can be read before the mark position becomes invalid
     * @throws IOException if the stream does not support mark(), or if some other I/O error occurs
     */
    @Throws(IOException::class)
    open suspend fun mark(readLimit: Int): Unit = throw IOException("mark not supported")

    /**
     * Tells whether this stream supports the mark() operation. The default implementation always returns false.
     *
     * @return true if this stream supports the mark() operation; false otherwise
     */
    open suspend fun markSupported(): Boolean = false

    /**
     * Reads a single character.
     *
     * Subclasses that intend to support efficient single-character input should override this method.
     *
     * @return The character read, as an integer in the range 0 to 65535 (0x00-0xffff), or -1 if the end of the stream
     * has been reached
     *
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    open suspend fun read(): Int {
        val buffer = CharArray(1)
        val read = read(buffer)
        return if (read == -1) -1 else buffer[0].code
    }

    /**
     * Reads characters into an array.
     *
     * @param b the buffer into which the data is read
     *
     * @return The total number of characters read into the buffer, or -1 if there is no more data because the end of
     * the stream has been reached
     *
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    open suspend fun read(b: CharArray): Int = read(b, 0, b.size)

    /**
     * Reads characters into a portion of an array.
     *
     * @param b the buffer into which the data is read
     * @param off the start offset in the destination array b
     * @param len the maximum number of characters read
     *
     * @return The total number of characters read into the buffer, or -1 if there is no more data because the end of
     * the stream has been reached
     */
    @Throws(IOException::class)
    open suspend fun read(b: CharArray, off: Int, len: Int): Int = read(CharBuffer.wrap(b, off, len))

    /**
     * Attempts to read characters into the specified character buffer. The buffer is used as a repository of characters
     * as-is: the only changes made are the results of a put operation. No flipping or rewinding of the buffer is
     * performed.
     *
     * @param b the buffer into which the data is read
     * @return The total number of characters read into the buffer, or -1 if there is no more data because the end of
     * the stream has been reached
     * @throws IOException if an I/O error occurs
     * @throws java.nio.ReadOnlyBufferException if the buffer is read-only
     */
    @Throws(IOException::class)
    abstract suspend fun read(b: CharBuffer): Int
    open suspend fun ready(): Boolean = true
    open suspend fun reset(): Unit = throw IOException("reset not supported")

    open suspend fun skip(n: Long): Long {
        require(n >= 0L) { "skip value is negative" }
        val nn = min(n, MAX_SKIP_BUFFER_SIZE).toInt()

        lock.withLock {
            if (skipBuffer == null || skipBuffer!!.size < nn) skipBuffer = CharArray(nn)

            var r = n
            while (r > 0) {
                val nc = read(skipBuffer!!, 0, min(r, nn.toLong()).toInt())
                if (nc == -1) break
                r -= nc
            }
            return n - r
        }
    }
}