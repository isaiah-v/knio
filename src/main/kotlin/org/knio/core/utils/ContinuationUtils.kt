package org.knio.core.utils

import java.nio.channels.CompletionHandler
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
 */
private class ContinuationCompletionHandler<T, R>(
    val onComplete: CompletionBlock<T, R>,
    val onFail: (Throwable) -> R
): CompletionHandler<T, Continuation<R>> {

    /**
     * Called when the operation completes successfully.
     *
     * @param result The result of the operation.
     * @param attachment The continuation to resume.
     */
    override fun completed(result: T, attachment: Continuation<R>) {
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
 * @param onFail The block to execute on failure.
 * @return A CompletionHandler that resumes a Continuation.
 */
internal fun <R> fromResult(onFail: (Throwable) -> R = DEFAULT_ON_FAIL): CompletionHandler<R, Continuation<R>> {
    return ContinuationCompletionHandler({ it }, onFail = onFail)
}

/**
 * Converts a result to a CompletionHandler that resumes a Continuation.
 *
 * @param T The type of the result.
 * @param R The type of the continuation result.
 * @param onFail The block to execute on failure.
 * @return A CompletionHandler that resumes a Continuation.
 */
internal fun <T,R> R.asCompletionHandler(onFail: (Throwable) -> R = DEFAULT_ON_FAIL): CompletionHandler<T, Continuation<R>> {
    return ContinuationCompletionHandler({ this }, onFail = onFail)
}

/**
 * Converts a CompletionBlock to a CompletionHandler that resumes a Continuation.
 *
 * @param T The type of the result.
 * @param R The type of the continuation result.
 * @param onFail The block to execute on failure.
 * @return A CompletionHandler that resumes a Continuation.
 */
internal fun <T, R> CompletionBlock<T, R>.asCompletionHandler(onFail: (Throwable) -> R = DEFAULT_ON_FAIL): CompletionHandler<T, Continuation<R>> {
    return ContinuationCompletionHandler(this, onFail = onFail)
}
