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
 * @return The `AsynchronousSocketChannel` representing the accepted connection.
 * @throws SocketTimeoutException if the timeout is reached before a connection is accepted.
 */
suspend fun AsynchronousServerSocketChannel.acceptSuspend(): AsynchronousSocketChannel = suspendCoroutine { continuation ->
    // Call the callback version of the non-blocking accept function, un-suspending the coroutine when complete.
    accept(continuation, fromResult())
}