package org.knio.core.nio

import org.knio.core.utils.fromResult
import org.knio.core.utils.timeout
import java.net.SocketTimeoutException
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import kotlin.coroutines.suspendCoroutine


/**
 * Suspends the coroutine and accepts a connection from the `AsynchronousServerSocketChannel`.
 *
 * @param timeout The timeout in milliseconds to wait for a connection. If `null`, the function will wait indefinitely.
 * @return The `AsynchronousSocketChannel` representing the accepted connection.
 * @throws SocketTimeoutException if the timeout is reached before a connection is accepted.
 */
suspend fun AsynchronousServerSocketChannel.acceptSuspend(
    timeout: Long? = null
): AsynchronousSocketChannel = suspendCoroutine { continuation ->

    // Create a timeout job if a timeout is specified.
    val timeoutJob = if(timeout != null) {
        continuation.timeout(timeout) { SocketTimeoutException() }
    } else {
        null
    }

    // Call the callback version of the non-blocking accept function, un-suspending the coroutine when complete.
    accept(continuation, fromResult(timeoutJob))
}