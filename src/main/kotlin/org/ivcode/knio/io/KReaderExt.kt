package org.ivcode.knio.io

import org.ivcode.knio.context.getKnioContext
import org.ivcode.knio.context.acquireReleasableCharBuffer

suspend fun KReader.readText(): String {
    val releasable = getKnioContext().byteBufferPool.acquireReleasableCharBuffer(1024)

    try {
        val buff = releasable.value

        val sb = StringBuilder()
        var read = this.read(buff)
        buff.flip()
        while (read != -1) {
            sb.append(buff)

            buff.clear()
            read = this.read(buff)
            buff.flip()
        }

        return sb.toString()
    } finally {
        releasable.release()
    }
}

suspend fun KReader.buffered(): KBufferedReader {
    return KBufferedReader.open(this)
}