package org.ivcode.knio.utils

import kotlinx.coroutines.withContext
import org.ivcode.knio.context.KnioContext
import kotlin.coroutines.CoroutineContext

/**
 * Executes the given block according to the context configuration for [@SynchronousNative] functions.
 *
 * @param T the type of the resource
 * @param R the return type of the block function
 * @param block a function to process this resource
 * @return the result of the block function
 * @throws Exception if an exception occurs during execution or closing
 */
suspend inline fun <T> nativeBlocking(knioContext: KnioContext, crossinline block: suspend ()->T):T {
    return knioContext.blockingContext.tryWith(block)
}

/**
 * Executes the given block function within the provided dispatcher context, if defined.
 * Otherwise, executes the block function on the current thread.
 *
 * @param T the type of the resource
 * @param R the return type of the block function
 * @param block a function to process this resource
 * @return the result of the block function
 * @throws Exception if an exception occurs during execution or closing
 */
suspend inline fun <T> CoroutineContext?.tryWith (crossinline block: suspend ()->T):T {
    return if (this == null) {
        block()
    } else {
        withContext(this) {
            block()
        }
    }
}