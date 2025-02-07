package org.knio.core.io

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.knio.core.context.KnioContext
import org.knio.core.context.getKnioContext
import org.knio.core.nio.writeSuspend
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * A `KFileOutputStream` is an asynchronous file output stream that supports coroutine-based
 * non-blocking I/O operations.
 *
 * This class is equivalent to the [java.io.FileOutputStream].
 *
 * @param path The path to the file to write.
 * @param context The context to use for I/O operations.
 */
class KFileOutputStream private constructor (
    path: Path,
    private val context: KnioContext
): KOutputStream() {

    private val fileChannel: AsynchronousFileChannel = context.channelFactory.openFileChannel(
        path,
        StandardOpenOption.WRITE
    )

    private val mutex = Mutex()
    private var position: Long = 0

    companion object {
        /**
         * Opens a `KFileOutputStream` for the given `Path`.
         *
         * @param path The `Path` to open the output stream for.
         * @return A `KFileOutputStream` for the given `Path`.
         */
        suspend fun open(path: Path): KFileOutputStream {
            return KFileOutputStream(path, getKnioContext())
        }
    }

    @Throws(IOException::class)
    override suspend fun write(b: ByteBuffer) = mutex.withLock {
        write0(b)
    }

    /**
     * Writes the remaining bytes in the `ByteBuffer` to the file.
     *
     * @param b The `ByteBuffer` to write.
     */
    @Throws(IOException::class)
    private suspend fun write0(b: ByteBuffer) {
        while (b.hasRemaining()) {
            val read = fileChannel.writeSuspend(b, position)

            if (read == -1 || read == 0) {
                break
            } else {
                position += read
            }
        }
    }

    /**
     * Closes this file output stream and releases any system resources associated with this stream.
     * This file output stream may no longer be used for writing bytes.
     *
     * If this stream has an associated channel then the channel is closed as well.
     */
    override suspend fun close() = mutex.withLock {
        close0()
    }

    /**
     * Closes the file channel without acquiring the mutex.
     */
    private suspend fun close0() {
        @Suppress("BlockingMethodInNonBlockingContext")
        fileChannel.close()
    }
}