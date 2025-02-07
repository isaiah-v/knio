package org.knio.core.io

import kotlinx.coroutines.sync.withLock
import org.knio.core.context.KnioContext
import org.knio.core.context.getKnioContext
import java.io.IOException
import java.nio.ByteBuffer
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
 * @property byteBufferPool the buffer pool to use for acquiring buffers
 * @property bufferSize the size of the buffer to use
 */
class KInputStreamReader private constructor (
    private val inputStream: KInputStream,
    charset: Charset = Charsets.UTF_8,
    private val bufferSize: Int = 8192,
    context: KnioContext
): KReader(context) {

    companion object {
        suspend fun open(inputStream: KInputStream, charset: Charset = Charsets.UTF_8): KInputStreamReader {
            return KInputStreamReader(inputStream, charset, context = getKnioContext())
        }
    }

    private val buffer: ByteBuffer = context.byteBufferPool.acquire(bufferSize).flip()
    private val decoder = charset.newDecoder()
    private var eof = false
    private var isClosed = false

    /**
     * The name of the character encoding being used by this stream.
     */
    val encoding: String
        get() = decoder.charset().name()

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
     * Closes this reader and releases any system resources associated with it.
     *
     * @throws IOException if an I/O error occurs
     */
    override suspend fun close() = lock.withLock {
        if (isClosed) {
            return
        }
        isClosed = true
        context.byteBufferPool.release(buffer)
        inputStream.close()
    }

}