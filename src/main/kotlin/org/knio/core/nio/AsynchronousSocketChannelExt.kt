package org.knio.core.nio

import org.knio.core.utils.fromResult
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.InterruptedByTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Handles errors by throwing a `SocketTimeoutException` if the error is an `InterruptedByTimeoutException`,
 * otherwise rethrows the original exception.
 *
 * @param e The throwable to handle.
 * @return Nothing, as this function always throws an exception.
 * @throws SocketTimeoutException if the throwable is an `InterruptedByTimeoutException`.
 * @throws Throwable the original throwable if it is not an `InterruptedByTimeoutException`.
 */
private fun <T> errorHandler(e: Throwable): T {
    when (e) {
        is InterruptedByTimeoutException -> throw SocketTimeoutException("Connection timed out")
        is IOException -> throw SocketException(e.message, e)
        else -> throw e
    }
}

/**
 * Suspends the coroutine and reads data from the `AsynchronousSocketChannel` into the provided `ByteBuffer`.
 *
 * @param b The `ByteBuffer` to read data into.
 * @param timeout The timeout in milliseconds, or `null` for no timeout.
 * @return The number of bytes read.
 * @throws SocketTimeoutException if the read operation times out.
 * @throws Throwable if any other error occurs during the read operation.
 */
suspend fun AsynchronousSocketChannel.readSuspend(
    b: ByteBuffer,
    timeout: Long? = null
): Int = suspendCoroutine {
    try {

        // Call the callback version of the non-blocking read function, un-suspending the coroutine when complete.
        if (timeout != null && timeout > 0) {
            // with timeout
            read(b, timeout, TimeUnit.MILLISECONDS, it, fromResult(onFail = ::errorHandler))
        } else {
            // without timeout
            read(b, it, fromResult(onFail = ::errorHandler))
        }

    } catch (e: Throwable) {
        it.resumeWithException(e)
    }
}

/**
 * Suspends the coroutine and writes data from the provided `ByteBuffer` to the `AsynchronousSocketChannel`.
 *
 * @param b The `ByteBuffer` containing data to write.
 * @param timeout The timeout in milliseconds, or `null` for no timeout.
 * @return The number of bytes written.
 * @throws SocketTimeoutException if the write operation times out.
 * @throws Throwable if any other error occurs during the write operation.
 */
suspend fun AsynchronousSocketChannel.writeSuspend(
    b: ByteBuffer,
    timeout: Long? = null
): Int = suspendCoroutine {
    try {
        // Call the callback version of the non-blocking write function, un-suspending the coroutine when complete.
        if (timeout != null && timeout > 0) {
            // with timeout
            write(b, timeout, TimeUnit.MILLISECONDS, it, fromResult(onFail = ::errorHandler))
        } else {
            // without timeout
            write(b, it, fromResult(onFail = ::errorHandler))
        }

    } catch (e: Throwable) {
        it.resumeWithException(e)
    }
}