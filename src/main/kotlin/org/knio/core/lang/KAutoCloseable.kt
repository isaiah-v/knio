package org.knio.core.lang

import kotlin.Throws

/**
 * An interface representing a closeable resource that can be closed asynchronously.
 *
 * Similar to [AutoCloseable], but with a suspendable [close] method.
 *
 * @See [AutoCloseable]
 * @See [use]
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