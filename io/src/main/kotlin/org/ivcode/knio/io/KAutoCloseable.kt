package org.ivcode.knio.io

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