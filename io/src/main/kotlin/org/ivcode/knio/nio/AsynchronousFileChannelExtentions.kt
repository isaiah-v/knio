package org.ivcode.org.ivcode.knio.nio

import org.ivcode.knio.utils.fromResult
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal suspend fun AsynchronousFileChannel.readSuspend(dst: ByteBuffer, position: Long): Int = suspendCoroutine {
    try {
        read(dst, position, it, fromResult())
    } catch (e: Throwable) {
        it.resumeWithException(e)
    }
}