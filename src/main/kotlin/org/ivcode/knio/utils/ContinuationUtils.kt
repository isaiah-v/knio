package org.ivcode.knio.utils

import kotlinx.coroutines.*
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeoutException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val DEFAULT_ON_FAIL: (Throwable) -> Nothing = { throw it }

internal typealias CompletionBlock<T, R> = (T) -> R

/**
 * A CompletionHandler implementation that resumes a Continuation with the result or exception.
 *
 * @param T The type of the result.
 * @param R The type of the continuation result.
 * @property onComplete The block to execute on completion.
 * @property onFail The block to execute on failure.
 * @property timeoutJob The job to cancel on completion or failure.
 */
private class ContinuationCompletionHandler<T, R>(
    val onComplete: CompletionBlock<T, R>,
    val onFail: (Throwable) -> R,
    val timeoutJob: Job?
): CompletionHandler<T, Continuation<R>> {

    /**
     * Called when the operation completes successfully.
     *
     * @param result The result of the operation.
     * @param attachment The continuation to resume.
     */
    override fun completed(result: T, attachment: Continuation<R>) {
        timeoutJob?.cancel()
        try {
            attachment.resume(onComplete(result))
        } catch (e: Throwable) {
            attachment.resumeWithException(e)
        }
    }

    /**
     * Called when the operation fails.
     *
     * @param exc The exception that caused the failure.
     * @param attachment The continuation to resume with the exception.
     */
    override fun failed(exc: Throwable, attachment: Continuation<R>) {
        timeoutJob?.cancel()
        try {
            attachment.resume(onFail(exc))
        } catch (e: Throwable) {
            attachment.resumeWithException(e)
        }
    }
}

/**
 * Creates a CompletionHandler that resumes a Continuation with the result or exception.
 *
 * @param R The type of the continuation result.
 * @param timeoutJob The job to cancel on completion or failure.
 * @param onFail The block to execute on failure.
 * @return A CompletionHandler that resumes a Continuation.
 */
internal fun <R> fromResult(timeoutJob: Job? = null, onFail: (Throwable) -> R = DEFAULT_ON_FAIL): CompletionHandler<R, Continuation<R>> {
    return ContinuationCompletionHandler({ it }, onFail = onFail, timeoutJob = timeoutJob)
}

/**
 * Converts a result to a CompletionHandler that resumes a Continuation.
 *
 * @param T The type of the result.
 * @param R The type of the continuation result.
 * @param timeoutJob The job to cancel on completion or failure.
 * @param onFail The block to execute on failure.
 * @return A CompletionHandler that resumes a Continuation.
 */
internal fun <T,R> R.asCompletionHandler(timeoutJob: Job? = null, onFail: (Throwable) -> R = DEFAULT_ON_FAIL): CompletionHandler<T, Continuation<R>> {
    return ContinuationCompletionHandler({ this }, onFail = onFail ,timeoutJob = timeoutJob)
}

/**
 * Converts a CompletionBlock to a CompletionHandler that resumes a Continuation.
 *
 * @param T The type of the result.
 * @param R The type of the continuation result.
 * @param timeoutJob The job to cancel on completion or failure.
 * @param onFail The block to execute on failure.
 * @return A CompletionHandler that resumes a Continuation.
 */
internal fun <T, R> CompletionBlock<T, R>.asCompletionHandler(timeoutJob: Job? = null, onFail: (Throwable) -> R = DEFAULT_ON_FAIL): CompletionHandler<T, Continuation<R>> {
    return ContinuationCompletionHandler(this, onFail = onFail, timeoutJob = timeoutJob)
}

/**
 * Sets a timeout for a Continuation.
 *
 * @param R The type of the continuation result.
 * @param timeout The timeout duration in milliseconds.
 * @param scope The CoroutineScope to launch the timeout job in.
 * @param exc The exception to throw on timeout.
 * @return A Job representing the timeout.
 */
@OptIn(DelicateCoroutinesApi::class)
internal fun <R> Continuation<R>.timeout (
    timeout: Long,
    scope: CoroutineScope = GlobalScope,
    exc: () -> Throwable = { TimeoutException() }
): Job = scope.launch {
    delay(timeout)
    resumeWithException(exc())
}