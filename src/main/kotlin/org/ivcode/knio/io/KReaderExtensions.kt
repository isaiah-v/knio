package org.ivcode.org.ivcode.knio.io

import org.ivcode.knio.context.knioContext
import org.ivcode.knio.io.KReader
import org.ivcode.org.ivcode.knio.context.acquireReleasableCharBuffer

suspend fun KReader.readText(): String {
    val releasable = knioContext().byteBufferPool.acquireReleasableCharBuffer(1024)

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