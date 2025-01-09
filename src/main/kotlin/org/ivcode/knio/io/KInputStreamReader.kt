package org.ivcode.knio.io

import kotlinx.coroutines.sync.withLock
import org.ivcode.knio.system.ByteBufferPool
import java.io.IOException
import java.nio.CharBuffer
import java.nio.ReadOnlyBufferException
import java.nio.charset.Charset
import java.nio.charset.CoderResult

/**
 * A reader that reads characters from an [KInputStream].
 *
 * This class is equivalent to the [java.io.InputStreamReader].
 *
 * @property inputStream the input stream to read from
 * @property bufferPool the buffer pool to use for acquiring buffers
 * @property bufferSize the size of the buffer to use
 */
class KInputStreamReader (
    private val inputStream: KInputStream,
    charset: Charset = Charsets.UTF_8,
    private val bufferPool: ByteBufferPool = ByteBufferPool.getDefault(),
    bufferSize: Int = 8192
): KReader() {

    private val buffer = bufferPool.acquire(bufferSize).apply { clear().flip() }
    private val decoder = charset.newDecoder()
    private var eof = false
    private var isClosed = false

    /**
     * Reads characters into the given [CharBuffer].
     *
     * @param b the buffer to read characters into
     * @return the number of characters read, or -1 if the end of the stream has been reached
     * @throws ReadOnlyBufferException if the buffer is read-only
     * @throws IOException if an I/O error occurs
     */
    override suspend fun read(b: CharBuffer): Int {
        lock.withLock {
            if (b.isReadOnly) {
                throw ReadOnlyBufferException()
            }

            val startPosition = b.position()
            var r: CoderResult
            do {
                r = decoder.decode(buffer, b, eof)
                when {
                    r.isMalformed -> throw IOException("malformed input")
                    r.isUnmappable -> throw IOException("unmappable input")
                    r.isOverflow -> {
                        return b.position() - startPosition
                    }

                    r.isUnderflow -> {
                        if (b.position() > startPosition) {
                            return b.position() - startPosition
                        }

                        if (eof) {
                            if (buffer.hasRemaining()) {
                                throw IOException("unexpected end of stream")
                            }
                            return -1
                        }

                        buffer.apply {
                            if (hasRemaining()) compact() else clear()
                        }

                        if (inputStream.read(buffer) == -1) {
                            eof = true
                        }
                        buffer.flip()
                    }
                }
            } while (!r.isError)

            return if (r.length() == 0 && eof) -1 else r.length()
        }
    }

    /**
     * Returns the name of the character encoding being used by this stream.
     *
     * @return the name of the character encoding being used by this stream
     */
    fun getEncoding(): String {
        return decoder.charset().name()
    }

    /**
     * Closes this reader and releases any system resources associated with it.
     *
     * @throws IOException if an I/O error occurs
     */
    override suspend fun close() {
        lock.withLock {
            if (isClosed) {
                return
            }
            isClosed = true
            bufferPool.release(buffer)
            inputStream.close()
        }
    }
}