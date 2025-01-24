package org.ivcode.knio.io

import kotlinx.coroutines.sync.withLock
import org.ivcode.knio.context.KnioContext
import org.ivcode.knio.context.acquireReleasableCharBuffer
import org.ivcode.knio.context.getKnioContext
import java.io.IOException
import java.nio.CharBuffer
import java.util.*

private const val UNMARKED = -1

/**
 * A buffered reader that reads characters from a KReader.
 *
 * @param reader The KReader to read characters from.
 * @param bufferSize The size of the buffer to use.
 */
class KBufferedReader(
    reader: KReader,
    bufferSize: Int,
    context: KnioContext
): KReader(context) {

    companion object {

        /**
         * Opens a buffered reader that reads characters from a [KReader].
         *
         * @param reader The KReader to read characters from.
         * @param bufferSize The size of the buffer to use. If null, the default buffer size will be used.
         *
         * @return The buffered reader.
         */
        suspend fun open(reader: KReader, bufferSize: Int? = null): KBufferedReader {
            val context = getKnioContext()
            val buffSize = bufferSize ?: context.maxStreamBufferSize

            return KBufferedReader(reader, buffSize, context)
        }
    }

    private var inStream: KReader? = reader
    private var buffer = context.byteBufferPool.acquireReleasableCharBuffer(bufferSize).apply { value.flip() }

    private var mark: Int = UNMARKED
    private var readAhead: Int = 0 /* Valid only when markedChar > 0 */


    /** Checks to make sure that the stream has not been closed  */
    @Throws(IOException::class)
    private fun ensureOpen() {
        if (buffer.released) throw IOException("Stream closed")
    }

    /**
     * Fills the input buffer, taking the mark into account if it is valid.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    private suspend fun fill(): Int {
        // not synchronized, as only called from other synchronized methods

        if(inStream == null) {
            return -1
        }

        val buffer = buffer.value
        var delta = 0
        if(isMarked()) {
            delta = buffer.position() - mark
            val readAheadLimit = readAhead + mark
            if(delta >= readAheadLimit) {
                // delta is larger than the readAhead limit, invalidate the mark
                mark = UNMARKED
                buffer.clear()
            } else {
                buffer.position(mark)
                buffer.compact()
                mark = 0
                buffer.position(delta)
            }
        } else {
            buffer.clear()
        }

        val read = inStream!!.read(buffer)
        if(read == -1) {
            // Nothing read, EOF reached, clean up, and return
            inStream = null
        }
        buffer.flip()

        if(isMarked()) {
            buffer.position(buffer.position() + delta)
        }

        return read
    }

    /**
     * Reads a single character.
     *
     * @return The character read, as an integer in the range 0 to 65535 (0x00-0xffff), or -1 if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun read(): Int {
        lock.withLock {
            ensureOpen()
            val buffer = buffer.value
            if(!buffer.hasRemaining()) {
                if(fill()==-1) return -1
            }

            return buffer.get().code
        }
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param cbuf The destination buffer.
     * @param off The offset at which to start storing characters.
     * @param len The maximum number of characters to read.
     * @return The number of characters read, or -1 if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun read(cbuf: CharArray, off: Int, len: Int): Int {
        lock.withLock {
            ensureOpen()

            val buffer = buffer.value
            var read = 0
            var eof = false

            while (read!=len && !eof) {
                if(buffer.hasRemaining()) {
                    val toRead = minOf(len-read, buffer.remaining())
                    buffer.get(cbuf, read+off, toRead)
                    read += toRead
                } else {
                    if(fill() == -1) {
                        eof = true
                    }
                }
            }

            return if(read==0 && eof) -1 else read
        }
    }

    /**
     * Reads characters into a CharBuffer.
     *
     * @param b The CharBuffer to read characters into.
     * @return The number of characters read, or -1 if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    override suspend fun read(b: CharBuffer): Int {
        lock.withLock {
            ensureOpen()

            val buffer = buffer.value

            var read = 0
            var eof = false

            while(b.hasRemaining() && !eof) {
                if(!buffer.hasRemaining()) {
                    if(fill() == -1) eof = true
                } else {
                    val toRead = minOf(b.remaining(), buffer.remaining())
                    b.put(b.position(), buffer, buffer.position(), toRead)
                    b.position(b.position() + toRead)
                    buffer.position(buffer.position() + toRead)

                    read += toRead
                }
            }

            return if(read==0 && eof) -1 else read
        }
    }

    /**
     * Reads a line of text. A line is considered to be terminated by any one of a line feed ('\n'), a carriage return ('\r'), or a carriage return followed immediately by a linefeed.
     *
     * @param ignoreLF If true, the next '\n' will be skipped.
     * @return A String containing the contents of the line, not including any line-termination characters, or null if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun readLine(): String? {
        val s: StringBuilder = StringBuilder()

        lock.withLock {
            ensureOpen()

            val buffer = buffer.value

            bufferLoop@ while (true) {
                if(!buffer.hasRemaining()) {
                    if(fill() == -1) {
                        return if (s.isNotEmpty()) {
                            s.toString()
                        } else{
                            null
                        }
                    }
                }

                var eol = false
                var c: Char

                charLoop@ while (buffer.hasRemaining()) {
                    c = buffer.get()
                    if (c == '\n') {
                        eol = true
                        break@charLoop
                    } else if (c == '\r') {
                        eol = true
                        if (buffer.hasRemaining() && buffer.get() != '\n') {
                            buffer.position(buffer.position() - 1)
                        }
                        break@charLoop
                    } else {
                        s.append(c)
                    }
                }

                if (eol) {
                    return s.toString()
                }
            }
        }
    }


    /**
     * Skips characters.
     *
     * @param n The number of characters to skip.
     * @return The number of characters actually skipped.
     * @throws IllegalArgumentException If `n` is negative.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun skip(n: Long): Long {
        require(n >= 0L) { "skip value is negative" }

        lock.withLock {
            ensureOpen()

            val buffer = buffer.value

            var r = n
            while (r > 0) {
                if(!buffer.hasRemaining()) {
                    if(fill() == -1) {
                        break
                    }
                }


                if(buffer.remaining() >= r) {
                    buffer.position(buffer.position() + r.toInt())
                    r = 0
                    break
                } else {
                    r -= buffer.remaining()
                    buffer.position(buffer.limit())
                }
            }
            return n - r
        }
    }

    /**
     * Tells whether this stream is ready to be read. A buffered character stream is ready if the buffer is not empty,
     * or if the underlying character stream is ready.
     *
     * @return `true` if the stream is ready to be read, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun ready(): Boolean {
        lock.withLock {
            ensureOpen()
            return buffer.value.hasRemaining() || inStream!!.ready()
        }
    }

    /**
     * Tells whether this stream supports the mark() operation, which it does.
     *
     * @return True if the stream supports the mark() operation, false otherwise.
     */
    override suspend fun markSupported(): Boolean = true

    /**
     * Marks the present position in the stream. Subsequent calls to reset() will attempt to reposition the stream to
     * this point.
     *
     * @param readLimit  Limit on the number of characters that may be read while still preserving the mark. An attempt
     * to reset the stream after reading characters up to this limit or beyond may fail. A limit value larger than the size of the input buffer will cause a new buffer to be allocated whose size is no smaller than limit. Therefore large values should be used with care.
     *
     * @throws IllegalArgumentException If readAheadLimit is < 0.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun mark(readLimit: Int) {
        require(readLimit >= 0) { "Read-ahead limit < 0" }

        lock.withLock {
            ensureOpen()
            setMark(readLimit)
        }
    }

    /**
     * Resets the stream to the most recent mark.
     *
     * @throws IOException If the stream has never been marked, or if the mark has been invalidated.
     */
    @Throws(IOException::class)
    override suspend fun reset() {
        lock.withLock {
            ensureOpen()
            if (!resetToMark()) {
                throw IOException("Mark invalid")
            }
        }
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun close() {
        lock.withLock {
            if (inStream == null) return

            inStream!!.close()
            inStream = null

            buffer.release()
        }
    }


    private fun isMarked(): Boolean {
        return mark > UNMARKED
    }

    private fun setMark(readLimit: Int): Int {
        if(buffer.value.limit() < readLimit) {
            buffer.resize(readLimit)
        }

        val mark = this.buffer.value.position()

        this.mark = mark
        this.readAhead = readLimit
        return mark
    }

    private fun resetToMark(): Boolean {
        if(!isMarked()) {
            return false
        }

        this.buffer.value.position(mark)
        return true
    }
}