package org.ivcode.knio.system

import org.ivcode.knio.io.KAutoCloseable

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
