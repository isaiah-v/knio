package org.ivcode.knio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * A coroutine-based asynchronous equivalent of [java.io.FileInputStream].
 *
 * This class is not thread-safe and should be used in a single-threaded context
 * or with external synchronization if shared between threads.
 *
 * @param path The path to the file to read.
 */
class KFileInputStream (
    path: Path
): KInputStream() {

    /** The asynchronous file channel for reading data. */
    private val channel: AsynchronousFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)

    /** The current position in the file. */
    private var position: Long = 0

    /** The mark position in the file. */
    private var markPosition: Long? = null

    /** The mark limit for the read-ahead limit. */
    private var markLimit: Int = 0

    /** The completion handler for asynchronous read operations. */
    private val callback = object : CompletionHandler<Int, Continuation<Int>> {
        override fun completed(result: Int, attachment: Continuation<Int>) {
            if(result > 0) {
                position += result
            }
            attachment.resume(result)
        }

        override fun failed(exc: Throwable, attachment: Continuation<Int>) {
            attachment.resumeWithException(exc)
        }
    }

    /**
     * Returns the number of bytes available to read.
     *
     * @return The number of bytes available.
     */
    override suspend fun available (): Int = withContext(Dispatchers.IO) {
        (channel.size() - position).toInt()
    }

    /**
     * Marks the current position in the input stream.
     *
     * @param readLimit The maximum limit of bytes that can be read before the mark position becomes invalid.
     */
    override suspend  fun mark(readLimit: Int) {
        markPosition = position
        markLimit = readLimit
    }

    /**
     * Checks if mark and reset are supported.
     *
     * @return True if mark and reset are supported, false otherwise.
     */
    override suspend  fun markSupported(): Boolean {
        return true
    }

    override suspend fun read(b: ByteBuffer): Int {
        try {
            val result = doRead(b)
            return result
        } catch (e: IOException) {
            this.close()
            throw IOException("Read failed", e)
        }
    }

    /**
     * Resets the input stream to the previously marked position.
     *
     * @throws IOException If the mark position is invalid.
     */
    override suspend fun reset() {
        val markPosition = markPosition ?: throw IOException("Mark not set")

        if(position < markPosition || position - markPosition > markLimit) {
            throw IOException("Mark invalid")
        }

        position = markPosition
    }

    /**
     * Skips over and discards the specified number of bytes from the input stream.
     *
     * @param n The number of bytes to skip.
     * @return The actual number of bytes skipped.
     */
    override suspend fun skip(n: Long): Long {
        if (n <= 0) return 0

        val remaining = maxOf(0, available().toLong())
        val skip = minOf(n, remaining)
        position += skip
        return skip
    }

    /**
     * Performs the actual read operation.
     *
     * @return The number of bytes read, or -1 if the end of the file is reached.
     */
    private suspend fun doRead(buffer: ByteBuffer): Int = suspendCoroutine { continuation ->
        try {
            channel.read(buffer, position, continuation, callback)
        } catch (e: Throwable) {
            continuation.resumeWithException(e)
        }
    }

    /**
     * Closes the file input stream.
     */
    override suspend fun close () = withContext(Dispatchers.IO) {
        channel.close()
    }
}