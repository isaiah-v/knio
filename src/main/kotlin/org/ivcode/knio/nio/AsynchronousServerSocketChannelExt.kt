package org.ivcode.knio.nio

import org.ivcode.knio.utils.fromResult
import org.ivcode.knio.utils.timeout
import java.net.SocketTimeoutException
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import kotlin.coroutines.suspendCoroutine

suspend fun AsynchronousServerSocketChannel.acceptSuspend(
    timeout: Long? = null
): AsynchronousSocketChannel = suspendCoroutine { continuation ->
    val timeoutJob = if(timeout != null) {
        continuation.timeout(timeout) { SocketTimeoutException() }
    } else {
        null
    }
    accept(continuation, fromResult(timeoutJob))
}