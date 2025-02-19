package org.knio.core.io

import org.knio.core.context.KnioContext
import org.knio.core.lang.KAutoCloseable
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * This abstract class is the superclass of all classes representing non-blocking input stream of bytes.
 *
 * Applications that need to define a subclass of [KInputStream] must always provide a method that performs a bulk read.
 *
 * @see InputStream
 */
abstract class KInputStream(
    protected val context: KnioContext
): KAutoCloseable {

    @Throws(IOException::class)
    abstract suspend fun read(b: ByteBuffer): Int

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without
     * suspending for an I/O operation, which may be 0, or 0 when end of stream is detected.
     *
     * @return The number of bytes that can be read from this input stream without suspending.
     * @throws IOException if an I/ O error occurs.
     * @see InputStream.available
     */
    @Throws(IOException::class)
    open suspend fun available(): Int = 0

    /**
     * Skips over and discards `n` bytes of data from this input stream. The `skip` method may, for a variety of
     * reasons, end up skipping over some smaller number of bytes, possibly `0`. This may result from any of a number of
     * conditions; reaching end of file before `n` bytes have been skipped is only one possibility. The actual number of
     * bytes skipped is returned. If `n` is negative, the skip method for class `InputStream` always returns `0`, and no
     * bytes are skipped. Subclasses may handle the negative value differently.
     *
     * @param n The number of bytes to be skipped.
     * @return The actual number of bytes skipped.
     * @throws IOException If an I/O error occurs.
     * @see InputStream.skip
     */
    @Throws(IOException::class)
    open suspend fun skip(n: Long): Long {
        var remaining = n

        if (n <= 0) {
            return 0
        }

        val bufferSize = minOf(context.maxTaskBufferSize.toLong(), n).toInt()
        val buffer = context.byteBufferPool.acquire(bufferSize)

        try {
            while (remaining > 0) {
                val nr = read(buffer)
                if (nr < 0) {
                    break
                }
                remaining -= nr.toLong()
            }
        } finally {
            context.byteBufferPool.release(buffer)
        }

        return n - remaining
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an int in the range 0 to 255. If
     * no byte is available because the end of the stream has been reached, the value -1 is returned. This method blocks
     * until input data is available, the end of the stream is detected, or an exception is thrown.
     *
     * @return The next byte of data, or -1 if the end of the stream is reached.
     * @throws IOException If an I/O error occurs.
     * @see InputStream.read
     */
    @Throws(IOException::class)
    open suspend fun read(): Int {
        val buffer = ByteArray(1)
        return read(buffer).takeIf { it != -1 }?.let { buffer[0].toInt() and 0xFF } ?: -1
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes
     * actually read is returned as an integer. This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * If the length of b is zero, then no bytes are read and 0 is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at the end of the file, the value -1 is returned;
     * otherwise, at least one byte is read and stored into b.
     *
     * The first byte read is stored into element b[0], the next one into b[1], and so on. The number of bytes read is,
     * at most, equal to the length of b. Let k be the number of bytes actually read; these bytes will be stored in
     * elements b[0] through b[k-1], leaving elements b[k] through b[b.length-1] unaffected.
     *
     * @param b The buffer into which the data is read.
     *
     * @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     * stream has been reached.
     *
     * @throws IOException If an I/O error occurs.
     *
     * @see InputStream.read
     */
    @Throws(IOException::class)
    open suspend fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    /**
     * Reads up to len bytes of data from the input stream into an array of bytes. An attempt is made to read as many as
     * len bytes, but a smaller number may be read. The number of bytes actually read is returned as an integer.
     *
     * This method blocks until input data is available, end of file is detected, or an exception is thrown.
     *
     * If len is zero, then no bytes are read and 0 is returned; otherwise, there is an attempt to read at least one
     * byte. If no byte is available because the stream is at end of file, the value -1 is returned; otherwise, at least
     * one byte is read and stored into b.
     *
     * The first byte read is stored into element b[off], the next one into b[off+1], and so on. The number of bytes
     * read is, at most, equal to len. Let k be the number of bytes actually read; these bytes will be stored in
     * elements b[off] through b[off+k-1], leaving elements b[off+k] through b[off+len-1] unaffected.
     *
     * In every case, elements b[0] through b[off-1] and elements b[off+len] through b[b.length-1] are unaffected.
     *
     * @param b The buffer into which the data is read.
     * @param off The start offset in the destination array b.
     * @param len The maximum number of bytes read.
     * @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     * @see InputStream.read
     */
    @Throws(IOException::class)
    open suspend fun read(b: ByteArray, off: Int, len: Int): Int {
        if(off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException()
        }
        if (len == 0) {
            return 0
        }

        val buffer = ByteBuffer.wrap(b, off, len)
        return read(buffer)
    }


    /**
     * Reads the requested number of bytes from the input stream into the given byte array. This method blocks until len
     * bytes of input data have been read, end of stream is detected, or an exception is thrown. The number of bytes
     * actually read, possibly zero, is returned. This method does not close the input stream.
     * I
     * n the case where end of stream is reached before len bytes have been read, then the actual number of bytes read
     * will be returned. When this stream reaches end of stream, further invocations of this method will return zero.
     *
     * If len is zero, then no bytes are read and 0 is returned; otherwise, there is an attempt to read up to len bytes.
     *
     * The first byte read is stored into element b[off], the next one in to b[off+1], and so on. The number of bytes
     * read is, at most, equal to len. Let k be the number of bytes actually read; these bytes will be stored in
     * elements b[off] through b[off+k-1], leaving elements b[off+k ] through b[off+len-1] unaffected.
     *
     * The behavior for the case where the input stream is asynchronously closed, or the thread interrupted during the
     * read, is highly input stream specific, and therefore not specified.
     *
     * If an I/O error occurs reading from the input stream, then it may do so after some, but not all, bytes of b have
     * been updated with data from the input stream. Consequently, the input stream and b may be in an inconsistent
     * state. It is strongly recommended that the stream be promptly closed if an I/O error occurs.
     *
     * @param b The buffer into which the data is read.
     * @param off The start offset in the destination array b.
     * @param len The maximum number of bytes read.
     * @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     * stream has been reached.
     *
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException if b is null.
     * @throws IndexOutOfBoundsException if off is negative, len is negative, or len is greater than b.length - off
     *
     * @see InputStream.read
     */
    @Throws(IOException::class)
    open suspend fun readNBytes(b: ByteArray, off: Int, len: Int): Int {
        if(off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException()
        }
        if (len == 0) {
            return 0
        }

        var total = 0
        while (total < len) {
            val bytes = read(b, off + total, len - total)
            if (bytes < 0) {
                break
            }
            total += bytes
        }
        return total
    }

    /**
     * Reads up to a specified number of bytes from the input stream. This method reads until the requested number of
     * bytes has been read, end of stream is detected, or an exception is thrown. This method does not close the input
     * stream.
     *
     * The length of the returned array equals the number of bytes read from the stream. If len is zero, then no bytes
     * are read and an empty byte array is returned. Otherwise, up to len bytes are read from the stream. Fewer than len
     * bytes may be read if end of stream is encountered.
     *
     * When this stream reaches end of stream, further invocations of this method will return an empty byte array.
     *
     * Note that this method is intended for simple cases where it is convenient to read the specified number of bytes
     * into a byte array. The total amount of memory allocated by this method is proportional to the number of bytes
     * read from the stream which is bounded by len. Therefore, the method may be safely called with very large values
     * of len provided sufficient memory is available.
     *
     * The behavior for the case where the input stream is asynchronously closed, or the thread interrupted during the
     * read, is highly input stream specific, and therefore not specified.
     *
     * If an I/O error occurs reading from the input stream, then it may do so after some, but not all, bytes have been
     * read. Consequently the input stream may not be at end of stream and may be in an inconsistent state. It is
     * strongly recommended that the stream be promptly closed if an I/O error occurs.
     *
     * @param len The maximum number of bytes to read.
     * @return A byte array containing the bytes read from the stream.
     *
     * @throws IOException If an I/O error occurs.
     * @throws IllegalArgumentException if len is negative.
     *
     * @see InputStream.readNBytes
     */
    @Throws(IOException::class)
    open suspend fun readNBytes(len: Int): ByteArray {
        val b = ByteArray(len)
        val bytesRead = readNBytes(b, 0, len)
        return b.copyOf(bytesRead)
    }

    /**
     * Reads all remaining bytes from the input stream. This method blocks until all remaining bytes have been read and
     * end of stream is detected, or an exception is thrown. This method does not close the input stream.
     *
     * When this stream reaches end of stream, further invocations of this method will return an empty byte array.
     *
     * Note that this method is intended for simple cases where it is convenient to read all bytes into a byte array. It
     * is not intended for reading input streams with large amounts of data.
     *
     * The behavior for the case where the input stream is asynchronously closed, or the thread interrupted during the
     * read, is highly input stream specific, and therefore not specified.
     *
     * If an I/O error occurs reading from the input stream, then it may do so after some, but not all, bytes have been
     * read. Consequently, the input stream may not be at end of stream and may be in an inconsistent state. It is
     * strongly recommended that the stream be promptly closed if an I/O error occurs.
     *
     * @return A byte array containing the bytes read from the stream.
     */
    @Throws(IOException::class)
    open suspend fun readAllBytes(): ByteArray {
        val buffer = context.byteBufferPool.acquire(context.maxTaskBufferSize)
        try {
            val out = ByteArrayOutputStream()
            var bytesRead = read(buffer)
            while (bytesRead >= 0) {
                buffer.flip()
                out.write(buffer.array(), buffer.position(), buffer.remaining())
                buffer.clear()
                bytesRead = read(buffer)
            }

            return out.toByteArray()
        } finally {
            context.byteBufferPool.release(buffer)
        }
    }


    /**
     * Tests if this input stream supports the mark and reset methods. Whether or not mark and reset are supported is an
     * invariant property of a particular input stream instance.
     *
     * @return True if this stream supports the mark and reset methods, false otherwise.
     */
    open suspend fun markSupported(): Boolean = false

    /**
     * Repositions this stream to the position at the time the mark method was last called on this input stream.
     * The general contract of reset is:
     *
     *  - If the method markSupported returns true, then:
     *    - If the method mark has not been called since the stream was created, or the number of bytes read from the
     *  stream since mark was last called is larger than the argument to mark at that last call, then an IOException
     *  might be thrown.
     *    - If such an IOException is not thrown, then the stream is reset to a state such that all the bytes read since
     *  the most recent call to mark (or since the start of the file, if mark has not been called) will be resupplied to
     *  subsequent callers of the read method, followed by any bytes that otherwise would have been the next input data
     *  as of the time of the call to reset.
     *
     *  - If the method markSupported returns false, then:
     *    - The call to reset may throw an IOException.
     *    - If an IOException is not thrown, then the stream is reset to a fixed state that depends on the particular type of the input stream and how it was created. The bytes that will be supplied to subsequent callers of the read method depend on the particular type of the input stream.
     */
    @Throws(IOException::class)
    open suspend fun reset(): Unit = throw IOException("Mark not supported")

    /**
     * Marks the current position in the input stream.
     *
     * The `readLimit` parameter is ignored.
     *
     * @param readLimit The maximum limit of bytes that can be read before the mark position becomes invalid.
     * @throws IOException if some other I/O error occurs.
     */
    open suspend fun mark(readLimit: Int) {}

    @Throws(IOException::class)
    override suspend fun close() {}
}