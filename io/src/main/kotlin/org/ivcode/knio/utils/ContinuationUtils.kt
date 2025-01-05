package org.ivcode.knio.utils

import kotlinx.coroutines.*
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeoutException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val DEFAULT_ON_FAIL: (Throwable) -> Nothing = { throw it }

typealias CompletionBlock<T, R> = (T) -> R

open class ContinuationCompletionHandler<T, R>(
    private val onComplete: CompletionBlock<T, R>,
    private val onFail: (Throwable) -> R,
    private val timeoutJob: Job?
): CompletionHandler<T, Continuation<R>> {
    override fun completed(result: T, attachment: Continuation<R>) {
        timeoutJob?.cancel()
        try {
            attachment.resume(onComplete(result))
        } catch (e: Throwable) {
            attachment.resumeWithException(e)
        }
    }

    override fun failed(exc: Throwable, attachment: Continuation<R>) {
        timeoutJob?.cancel()
        try {
            attachment.resume(onFail(exc))
        } catch (e: Throwable) {
            attachment.resumeWithException(e)
        }
    }
}

fun <R> fromResult(timeoutJob: Job? = null, onFail: (Throwable) -> R = DEFAULT_ON_FAIL): CompletionHandler<R, Continuation<R>> {
    return ContinuationCompletionHandler({ it }, onFail = onFail, timeoutJob = timeoutJob)
}

fun <T,R> R.asCompletionHandler(timeoutJob: Job? = null, onFail: (Throwable) -> R = DEFAULT_ON_FAIL): CompletionHandler<T, Continuation<R>> {
    return ContinuationCompletionHandler({ this }, onFail = onFail ,timeoutJob = timeoutJob)
}

fun <T, R> CompletionBlock<T, R>.asCompletionHandler(timeoutJob: Job? = null, onFail: (Throwable) -> R = DEFAULT_ON_FAIL): CompletionHandler<T, Continuation<R>> {
    return ContinuationCompletionHandler(this, onFail = onFail, timeoutJob = timeoutJob)
}

@OptIn(DelicateCoroutinesApi::class)
fun <R> Continuation<R>.timeout (
    timeout: Long,
    scope: CoroutineScope = GlobalScope,
    exc: () -> Throwable = { TimeoutException() }
): Job = scope.launch {
    delay(timeout)
    resumeWithException(exc())
}