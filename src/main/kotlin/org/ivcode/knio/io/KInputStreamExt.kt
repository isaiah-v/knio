package org.ivcode.knio.io

import org.ivcode.knio.annotations.JavaIO
import java.io.OutputStream
import java.nio.charset.Charset

suspend fun KInputStream.reader(charset: Charset = Charsets.UTF_8): KReader =
    KInputStreamReader.open(this, charset)

suspend fun KInputStream.bufferedReader(charset: Charset = Charsets.UTF_8): KBufferedReader =
    KBufferedReader(this.reader(charset))

/**
 * Copies this input stream to the specified output stream.
 *
 * @param output The output stream to copy to.
 * @param bufferSize The buffer size to use when copying.
 * @return The number of bytes copied.
 */
suspend fun KInputStream.copyTo(output: KOutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        output.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
    }
    return bytesCopied
}


/**
 * Copies this input stream to the specified output stream.
 *
 * NOTE: This function may block while writing to the legacy [java.io.OutputStream]. It is recommended to call this
 * function from an I/O dispatcher.
 *
 * @param output The output stream to copy to.
 * @param bufferSize The buffer size to use when copying.
 * @return The number of bytes copied.
 */
@JavaIO
suspend fun KInputStream.copyTo(output: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        @Suppress("BlockingMethodInNonBlockingContext")
        output.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
    }
    return bytesCopied
}
