package org.knio.core.nio

import org.knio.core.utils.fromResult
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Suspends the coroutine and reads data from the `AsynchronousFileChannel` into the provided `ByteBuffer` starting at
 * the given position.
 *
 * @param dst The `ByteBuffer` to read data into.
 * @param position The position in the file at which to start reading.
 * @return The number of bytes read.
 * @throws Throwable if any error occurs during the read operation.
 */
internal suspend fun AsynchronousFileChannel.readSuspend(dst: ByteBuffer, position: Long): Int = suspendCoroutine {
    try {
        // Call the callback version of the non-blocking read function, un-suspending the coroutine when complete.
        read(dst, position, it, fromResult())
    } catch (e: Throwable) {
        it.resumeWithException(e)
    }
}

/**
 * Suspends the coroutine and writes data from the `ByteBuffer` to the `AsynchronousFileChannel` starting at the given
 * position.
 *
 * @param src The `ByteBuffer` containing the data to write.
 * @param position The position in the file at which to start writing.
 * @return The number of bytes written.
 * @throws Throwable if any error occurs during the write operation.
 */
internal suspend fun AsynchronousFileChannel.writeSuspend(src: ByteBuffer, position: Long): Int = suspendCoroutine {
    try {
        // Call the callback version of the non-blocking write function, un-suspending the coroutine when complete.
        write(src, position, it, fromResult())
    } catch (e: Throwable) {
        it.resumeWithException(e)
    }
}