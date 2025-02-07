package org.knio.core.security

import org.knio.core.context.getKnioContext
import org.knio.core.io.KInputStream
import java.io.IOException
import java.security.MessageDigest
import kotlin.jvm.Throws

/**
 * Extension function to update a MessageDigest with data from a [KInputStream].
 *
 * This function reads data from the provided KInputStream in chunks and updates
 * the MessageDigest with the read data. It uses a buffer from the knioContext's
 * byteBufferPool to read the data.
 *
 * This function will consume the entire KInputStream but will not call [KInputStream.close].
 *
 * @param inputStream The KInputStream to read data from.
 * @throws IOException If an I/O error occurs.
 */
@Throws(IOException::class)
suspend fun MessageDigest.update(inputStream: KInputStream) {
    val buffer = getKnioContext().byteBufferPool.acquire(1024)
    try {
        while (true) {
            val read = inputStream.read(buffer)
            if (read == -1) {
                break
            }
            buffer.flip()
            update(buffer)
            buffer.clear()
        }
    } finally {
        getKnioContext().byteBufferPool.release(buffer)
    }
}