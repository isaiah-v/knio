package org.ivcode.knio.io

import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.nio.CharBuffer
import kotlin.math.min

/**
 * A buffered reader that reads characters from a KReader.
 *
 * @param reader The KReader to read characters from.
 * @param bufferSize The size of the buffer to use.
 */
class KBufferedReader(
    reader: KReader,
    bufferSize: Int = DEFAULT_CHAR_BUFFER_SIZE,
): KReader() {

    companion object {
        private const val INVALIDATED = -2
        private const val UNMARKED = -1
        private const val DEFAULT_CHAR_BUFFER_SIZE = 8192
        private const val DEFAULT_EXPECTED_LINE_LENGTH = 80
    }

    private var inStream: KReader? = reader
    private var cb: CharArray? = CharArray(bufferSize)
    private var nChars: Int = 0
    private var nextChar: Int = 0

    private var markedChar: Int = UNMARKED
    private var readAheadLimit: Int = 0 /* Valid only when markedChar > 0 */

    /** If the next character is a line feed, skip it  */
    private var skipLF: Boolean = false

    /** The skipLF flag when the mark was set  */
    private var markedSkipLF: Boolean = false

    /** Checks to make sure that the stream has not been closed  */
    @Throws(IOException::class)
    private fun ensureOpen() {
        if (inStream == null) throw IOException("Stream closed")
    }

    /**
     * Fills the input buffer, taking the mark into account if it is valid.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    private suspend fun fill() {
        val dst: Int
        if (markedChar <= UNMARKED) {
            /* No mark */
            dst = 0
        } else {
            /* Marked */
            val delta: Int = nextChar - markedChar
            if (delta >= readAheadLimit) {
                /* Gone past read-ahead limit: Invalidate mark */
                markedChar = INVALIDATED
                readAheadLimit = 0
                dst = 0
            } else {
                if (readAheadLimit <= cb!!.size) {
                    /* Shuffle in the current buffer */
                    System.arraycopy(cb, markedChar, cb, 0, delta)
                    markedChar = 0
                    dst = delta
                } else {
                    /* Reallocate buffer to accommodate read-ahead limit */
                    val ncb = CharArray(readAheadLimit)
                    System.arraycopy(cb, markedChar, ncb, 0, delta)
                    cb = ncb
                    markedChar = 0
                    dst = delta
                }
                nChars = delta
                nextChar = nChars
            }
        }

        var n: Int
        do {
            n = inStream!!.read(cb!!, dst, cb!!.size - dst)
        } while (n == 0)
        if (n > 0) {
            nChars = dst + n
            nextChar = dst
        }
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
            while (true) {
                if (nextChar >= nChars) {
                    fill()
                    if (nextChar >= nChars) return -1
                }
                if (skipLF) {
                    skipLF = false
                    if (cb!![nextChar] == '\n') {
                        nextChar++
                        continue
                    }
                }
                return cb!![nextChar++].code
            }
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
    private suspend fun read1(cbuf: CharArray, off: Int, len: Int): Int {
        if (nextChar >= nChars) {
            if (len >= cb!!.size && markedChar <= UNMARKED && !skipLF) {
                return inStream!!.read(cbuf, off, len)
            }
            fill()
        }
        if (nextChar >= nChars) return -1
        if (skipLF) {
            skipLF = false
            if (cb!![nextChar] === '\n') {
                nextChar++
                if (nextChar >= nChars) fill()
                if (nextChar >= nChars) return -1
            }
        }
        val n = min(len.toDouble(), (nChars - nextChar).toDouble()).toInt()
        System.arraycopy(cb, nextChar, cbuf, off, n)
        nextChar += n
        return n
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
            if ((off < 0) || (off > cbuf.size) || (len < 0) ||
                ((off + len) > cbuf.size) || ((off + len) < 0)
            ) {
                throw IndexOutOfBoundsException()
            } else if (len == 0) {
                return 0
            }

            var n = read1(cbuf, off, len)
            if (n <= 0) return n
            while ((n < len) && inStream!!.ready()) {
                val n1 = read1(cbuf, off + n, len - n)
                if (n1 <= 0) break
                n += n1
            }
            return n
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
        return TODO()
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
        var s: StringBuffer? = null
        var startChar: Int

        lock.withLock {
            ensureOpen()
            var omitLF = ignoreLF || skipLF
            bufferLoop@ while (true) {
                if (nextChar >= nChars) fill()
                if (nextChar >= nChars) { /* EOF */
                    return if (!s.isNullOrEmpty()) s.toString()
                    else null
                }
                var eol = false
                var c = 0.toChar()

                /* Skip a leftover '\n', if necessary */
                if (omitLF && (cb!![nextChar] === '\n')) nextChar++
                skipLF = false
                omitLF = false

                var i = nextChar
                charLoop@ while (i < nChars) {
                    c = cb!![i]
                    if ((c == '\n') || (c == '\r')) {
                        eol = true
                        break@charLoop
                    }
                    i++
                }
                startChar = nextChar
                nextChar = i

                if (eol) {
                    val str: String
                    if (s == null) {
                        str = String(cb!!, startChar, i - startChar)
                    } else {
                        s!!.append(cb!!, startChar, i - startChar)
                        str = s.toString()
                    }
                    nextChar++
                    if (c == '\r') {
                        skipLF = true
                    }
                    return str
                }

                if (s == null) s = StringBuffer(DEFAULT_EXPECTED_LINE_LENGTH)
                s!!.append(cb, startChar, i - startChar)
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
            if (skipLF) {
                if (nextChar >= nChars && inStream!!.ready()) {
                    fill()
                }
                if (nextChar < nChars) {
                    if (cb!![nextChar] == '\n') nextChar++
                    skipLF = false
                }
            }
            return (nextChar < nChars) || inStream!!.ready()
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
            this.readAheadLimit = readLimit
            markedChar = nextChar
            markedSkipLF = skipLF
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
            if (markedChar < 0) throw IOException(
                if (markedChar == INVALIDATED)
                    "Mark invalid"
                else
                    "Stream not marked"
            )
            nextChar = markedChar
            skipLF = markedSkipLF
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
            cb = null
        }
    }
}