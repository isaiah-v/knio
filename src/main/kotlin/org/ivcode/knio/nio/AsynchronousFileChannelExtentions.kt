package org.ivcode.knio.nio

import org.ivcode.knio.utils.fromResult
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Suspends the coroutine and reads data from the `AsynchronousFileChannel` into the provided `ByteBuffer` starting at the given position.
 *
 * @param dst The `ByteBuffer` to read data into.
 * @param position The position in the file at which to start reading.
 * @return The number of bytes read.
 * @throws Throwable if any error occurs during the read operation.
 */
internal suspend fun AsynchronousFileChannel.readSuspend(dst: ByteBuffer, position: Long): Int = suspendCoroutine {
    try {
        read(dst, position, it, fromResult())
    } catch (e: Throwable) {
        it.resumeWithException(e)
    }
}

internal suspend fun AsynchronousFileChannel.writeSuspend(src: ByteBuffer, position: Long): Int = suspendCoroutine {
    try {
        write(src, position, it, fromResult())
    } catch (e: Throwable) {
        it.resumeWithException(e)
    }
}