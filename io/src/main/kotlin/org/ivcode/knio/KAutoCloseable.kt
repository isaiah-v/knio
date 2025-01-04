package org.ivcode.knio

import kotlin.Throws

/**
 * An interface representing a closeable resource that can be closed asynchronously.
 */
interface KAutoCloseable {
    /**
     * Closes this resource, relinquishing any underlying resources.
     *
     * @throws Exception if an error occurs during closing
     */
    @Throws(Exception::class)
    suspend fun close()
}

/**
 * Executes the given block function on this resource and then closes it.
 *
 * @param T the type of the resource
 * @param R the return type of the block function
 * @param block a function to process this resource
 * @return the result of the block function
 * @throws Exception if an error occurs during closing
 */
@Throws(Exception::class)
suspend inline fun <T : KAutoCloseable, R> T.use(block: (T) -> R): R {
    return try {
        block(this)
    } finally {
        close()
    }
}