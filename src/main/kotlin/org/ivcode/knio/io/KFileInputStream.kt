package org.ivcode.knio.io

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ivcode.knio.context.KnioContext
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import org.ivcode.knio.nio.readSuspend
import org.ivcode.knio.context.getKnioContext
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.Throws

/**
 * A FileInputStream obtains input bytes from a file in a file system.
 *
 * This is a coroutine-based asynchronous equivalent of [java.io.FileInputStream].
 *
 * @param path The path to the file to read.
 */
class KFileInputStream private constructor(
    private val path: Path,
    context: KnioContext
): KInputStream(context) {

    private val mutex = Mutex()
    private val channel: AsynchronousFileChannel = context.channelFactory.openFileChannel(path, StandardOpenOption.READ)

    companion object {
        /**
         * Opens a file input stream for the specified file.
         *
         * @param path The path to the file to read.
         * @return The file input stream.
         */
        suspend fun open(path: Path): KFileInputStream {
            return KFileInputStream(path, getKnioContext())
        }

        /**
         * Opens a file input stream for the specified file path.
         *
         * @param path The path to the file to read as a String.
         * @return The file input stream.
         */
        suspend fun open(path: String): KFileInputStream {
            return open(Path.of(path))
        }
    }

    /** The current position in the file. */
    private var position: Long = 0

    /** The mark position in the file. */
    private var markPosition: Long? = null

    /** The mark limit for the read-ahead limit. */
    private var markLimit: Int = 0


    @Throws(IOException::class)
    override suspend fun available(): Int {
        // all reads perform I/O operations, so we can't know how many bytes are available without reading
        return 0
    }

    /**
     * Returns the total number of bytes in the file.
     *
     * @return The total number of bytes in the file.
     */
    @Throws(IOException::class)
    suspend fun size(): Long {
        @Suppress("BlockingMethodInNonBlockingContext")
        return channel.size()
    }

    private suspend fun remaining(): Long {
        return size() - position
    }


    @Throws(IOException::class)
    override suspend fun mark(readLimit: Int) {
        markPosition = position
        markLimit = readLimit
    }

    /**
     * Checks if mark and reset are supported.
     *
     * @return True if mark and reset are supported, false otherwise.
     */
    @Throws(IOException::class)
    override suspend fun markSupported(): Boolean {
        return true
    }

    /**
     * Reads bytes from the input stream into the specified ByteBuffer.
     *
     * An attempt is made to read `b.remaining()` bytes, but a smaller number may be read.
     *
     * @return The number of bytes read, or -1 if the end of the file is reached.
     */
    @Throws(IOException::class)
    override suspend fun read(b: ByteBuffer): Int = mutex.withLock {
        return read0(b)
    }

    private suspend fun read0(buffer: ByteBuffer): Int {
        val count = channel.readSuspend(buffer, position)
        if (count > 0) {
            position += count
        }

        return count
    }

    /**
     * Resets the input stream to the previously marked position.
     *
     * @throws IOException If the mark position is invalid.
     */
    @Throws(IOException::class)
    override suspend fun reset() = mutex.withLock {
        reset0()
    }

    private suspend fun reset0() {
        val markPosition = markPosition ?: throw IOException("Mark not set")

        if (position < markPosition || position - markPosition > markLimit) {
            throw IOException("Mark invalid")
        }

        position = markPosition
    }

    /**
     * Skips over and discards n bytes of data from the input stream.
     *
     * The skip method may, for a variety of reasons, end up skipping over some smaller number of bytes, possibly 0.
     * If n is negative, the method will try to skip backwards. The actual number of bytes skipped is returned.
     * If it skips forwards, it returns a positive value. If it skips backwards, it returns a negative value.
     *
     * @param n The number of bytes to skip.
     * @return The actual number of bytes skipped.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun skip(n: Long): Long = mutex.withLock {
        return skip0(n)
    }

    private suspend fun skip0(n: Long): Long {
        // This differs from the Java implementation in that will only skip up to the end of the file or the beginning.
        // It returns the number of skipped bytes, as the documentation states, rather than going past or throwing an
        // exception.

        if (n >= 0) {
            val skip = minOf(n, remaining())
            position += skip
            return skip
        } else {
            val rewind = -1 * minOf(-n, position)
            position += rewind
            return rewind
        }
    }

    /**
     * Closes this file input stream and releases any system resources associated with the stream.
     */
    @Throws(IOException::class)
    override suspend fun close() = mutex.withLock {
        close0()
    }

    /**
     * Closes the file input stream without locking the mutex.
     */
    private suspend fun close0() {
        @Suppress("BlockingMethodInNonBlockingContext")
        channel.close()
    }
}