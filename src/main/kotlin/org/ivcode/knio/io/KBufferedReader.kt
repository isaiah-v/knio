package org.ivcode.knio.io

import kotlinx.coroutines.sync.withLock
import org.ivcode.knio.context.KnioContext
import org.ivcode.knio.context.acquireReleasableCharBuffer
import org.ivcode.knio.context.getKnioContext
import java.io.IOException
import java.nio.CharBuffer

private const val DEFAULT_CHAR_BUFFER_SIZE = 8192

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
): KReader() {

    companion object {
        suspend fun open(reader: KReader, bufferSize: Int = DEFAULT_CHAR_BUFFER_SIZE): KBufferedReader {
            return KBufferedReader(reader, bufferSize, getKnioContext())
        }
    }

    private var inStream: KReader? = reader

    private var cb: CharArray? = CharArray(bufferSize)
    private var buffer = context.byteBufferPool.acquireReleasableCharBuffer(bufferSize).apply { value.flip() }

    private var mark: Int? = null

    private var nChars: Int = 0
    private var nextChar: Int = 0

    private var readAheadLimit: Int = 0 /* Valid only when markedChar > 0 */

    /** If the next character is a line feed, skip it  */
    private var skipLF: Boolean = false

    /** The skipLF flag when the mark was set  */
    private var markedSkipLF: Boolean = false

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

        if(isMarked()) {
            val delta = buffer.position() - mark!!
            if(delta > readAheadLimit) {
                mark = mark!! - delta
                buffer.compact()
            } else {
                mark = null
                buffer.clear()
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
            val buffer = buffer.value

            ensureOpen()
            if(!buffer.hasRemaining()) {
                if(fill()==-1) return -1
            }

            return buffer.get().code
        }
    }

    /**
     * Reads characters into a portion of an array, reading from the underlying stream if necessary.
     *
     * @param cbuf The destination buffer.
     * @param off The offset at which to start storing characters.
     * @param len The maximum number of characters to read.
     * @return The number of characters read, or -1 if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    private suspend fun read0(cbuf: CharArray, off: Int, len: Int): Int {
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
            return read0(cbuf, off, len)
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

    /**
     * Reads a line of text. A line is considered to be terminated by any one of a line feed ('\n'), a carriage return ('\r'), or a carriage return followed immediately by a linefeed.
     *
     * @param ignoreLF If true, the next '\n' will be skipped.
     * @return A String containing the contents of the line, not including any line-termination characters, or null if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun readLine(ignoreLF: Boolean = false): String? {
        var s: StringBuilder = StringBuilder()
        var startChar: Int

        lock.withLock {

            ensureOpen()
            var omitLF = ignoreLF || skipLF
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

                if (omitLF && buffer.get(buffer.position()) == '\n') {
                    buffer.position(buffer.position() + 1)
                }
                skipLF = false
                omitLF = false

                charLoop@ while (buffer.hasRemaining()) {
                    c = buffer.get()
                    if ((c == '\n') || (c == '\r')) {
                        eol = true
                        break@charLoop
                    } else {
                        s.append(c)
                    }

                }
                //startChar = nextChar
                //nextChar = i

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
            var r = n
            while (r > 0) {
                if (nextChar >= nChars) fill()
                if (nextChar >= nChars)  /* EOF */
                    break
                if (skipLF) {
                    skipLF = false
                    if (cb!![nextChar] == '\n') {
                        nextChar++
                    }
                }
                val d = (nChars - nextChar).toLong()
                if (r <= d) {
                    nextChar += r.toInt()
                    r = 0
                    break
                } else {
                    r -= d
                    nextChar = nChars
                }
            }
            return n - r
        }
    }

    /**
     * Tells whether this stream is ready to be read. A buffered character stream is ready if the buffer is not empty, or if the underlying character stream is ready.
     *
     * @return True if the stream is ready to be read, false otherwise.
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
     * Marks the present position in the stream. Subsequent calls to reset() will attempt to reposition the stream to this point.
     *
     * @param readLimit Limit on the number of characters that may be read while still preserving the mark.
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
            buffer.value.reset()
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
        return mark != null
    }

    private fun setMark(readLimit: Int): Int {
        val mark = this.buffer.value.position()

        this.mark = mark
        this.readAheadLimit = readLimit
        return mark
    }

    private fun resetToMark(): Boolean {
        val mark = mark ?: return false

        this.buffer.value.position(mark)
        return true
    }
}