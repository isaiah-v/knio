package org.knio.core.io

import org.knio.core.lang.KAutoCloseable
import java.io.IOException
import java.nio.ByteBuffer

abstract class KOutputStream: KAutoCloseable {

    /**
     * Writes the specified byte to this file output stream. Implements the write method of OutputStream.
     *
     * @param b the byte to be written.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    open suspend fun write(b: Int) = write(byteArrayOf(b.toByte()))

    /**
     * Writes `b.length` bytes from the specified byte array to this output stream. The general contract for `write(b)`
     * is that it should have exactly the same effect as the call `write(b, 0, b.length)`.
     *
     * @param b the data.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    open suspend fun write(b: ByteArray): Unit = write(b, 0, b.size)

    /**
     * Writes len bytes from the specified byte array starting at offset off to this file output stream. Implements the
     * write method of OutputStream.
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    open suspend fun write(b: ByteArray, off: Int, len: Int):Unit = write(ByteBuffer.wrap(b, off, len))

    /**
     * Writes `b.remaining()` bytes from the specified byte array to this output stream.
     *
     * @param b the data.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    abstract suspend fun write(b: ByteBuffer): Unit

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out.
     *
     * @throws IOException if an I/O error occurs.
     */
    open suspend fun flush() {}

    /**
     * Closes this output stream and releases any system resources associated with the stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    override suspend fun close() {}
}