package org.ivcode.org.ivcode.knio.nio

import org.ivcode.knio.utils.fromResult
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.InterruptedByTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private fun <T> errorHandler (e: Throwable): T {
    throw if (e is InterruptedByTimeoutException) {
        SocketTimeoutException("Connection timed out")
    } else {
        e
    }
}

internal suspend fun AsynchronousSocketChannel.readSuspend(b: ByteBuffer, timeout: Long?=null): Int = suspendCoroutine {
    try {
        if(timeout!=null && timeout > 0) {
            read(b, timeout, TimeUnit.MILLISECONDS, it, fromResult(onFail = ::errorHandler))
        } else {
            read(b, it, fromResult(onFail = ::errorHandler))
        }
    } catch (e: Throwable) {
        it.resumeWithException(e)
    }
}

internal suspend fun AsynchronousSocketChannel.writeSuspend(b: ByteBuffer, timeout: Long?=null): Int = suspendCoroutine {
    try {
        if(timeout!=null && timeout > 0) {
            write(b, timeout, TimeUnit.MILLISECONDS, it, fromResult(onFail = ::errorHandler))
        } else {
            write(b, it, fromResult(onFail = ::errorHandler))
        }
    } catch (e: Throwable) {
        it.resumeWithException(e)
    }
}
