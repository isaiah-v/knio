package org.knio.core.lang

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Executes the given block function on this resource and then closes it.
 *
 * @param T the type of the resource
 * @param R the return type of the block function
 * @param block a function to process this resource
 * @return the result of the block function
 * @throws Exception if an exception occurs during execution or closing
 */
@Throws(Exception::class)
suspend inline fun <T : KAutoCloseable, R> T.use(crossinline block: suspend (T) -> R): R {
    return try {
        block(this)
    } finally {
        close()
    }
}

/**
 * Executes the given block function on this resource within the provided dispatcher context and then closes it.
 *
 * @param T the type of the resource
 * @param R the return type of the block function
 * @param dispatcher the coroutine dispatcher to run the block function
 * @param block a suspend function to process this resource
 * @return the result of the block function
 * @throws Exception if an exception occurs during execution or closing
 */
@Throws(Exception::class)
suspend inline fun <T : KAutoCloseable, R> T.use(
    dispatcher: CoroutineDispatcher,
    crossinline block: suspend (T) -> R
): R = withContext(dispatcher) {
    return@withContext try {
        block(this@use)
    } finally {
        close()
    }
}

