package org.knio.core.io

import org.knio.core.context.KnioContext
import org.knio.core.context.getKnioContext
import java.nio.ByteBuffer

class KByteArrayInputStream(
    private val data: ByteArray,
    context: KnioContext
): KInputStream(context) {

    companion object {
        suspend fun open(data: ByteArray): KByteArrayInputStream {
            return KByteArrayInputStream(data, getKnioContext())
        }
    }

    private var position = 0

    override suspend fun read(b: ByteBuffer): Int {
        if (position >= data.size) {
            return -1
        }

        val remaining = b.remaining()
        if (remaining == 0) {
            return 0
        }

        val length = minOf(remaining, data.size - position)
        b.put(data, position, length)
        position += length
        return length
    }
}