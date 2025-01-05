package org.ivcode.knio.io

import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.min

abstract class KInputStream: KAutoCloseable {
    companion object {
        private const val MAX_SKIP_BUFFER_SIZE: Long = 2048
    }

    abstract suspend fun read(b: ByteBuffer): Int

    open suspend fun available(): Int = 0
    open suspend fun skip(n: Long): Long {
        var remaining = n
        var nr: Int

        if (n <= 0) {
            return 0
        }

        val size = MAX_SKIP_BUFFER_SIZE.coerceAtMost(remaining).toInt()
        val skipBuffer = ByteArray(size)
        while (remaining > 0) {
            nr = read(skipBuffer, 0, min(size.toDouble(), remaining.toDouble()).toInt())
            if (nr < 0) {
                break
            }
            remaining -= nr.toLong()
        }

        return n - remaining
    }

    open suspend fun read(): Int {
        val buffer = ByteArray(1)
        return read(buffer).takeIf { it != -1 }?.let { buffer[0].toInt() and 0xFF } ?: -1
    }
    open suspend fun read(b: ByteArray) = read(b, 0, b.size)
    open suspend fun read(b: ByteArray, off: Int, len: Int): Int {
        val buffer = ByteBuffer.wrap(b, off, len)
        return read(buffer)
    }

    open suspend fun markSupported(): Boolean = false
    open suspend fun reset(): Unit = throw IOException("Mark not supported")
    open suspend fun mark(readLimit: Int) {}
    override suspend fun close() {}
}