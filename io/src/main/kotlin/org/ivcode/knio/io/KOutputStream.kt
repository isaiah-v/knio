package org.ivcode.knio.io

import org.ivcode.knio.lang.KAutoCloseable
import java.nio.ByteBuffer

abstract class KOutputStream: KAutoCloseable {
    abstract suspend fun write(b: ByteBuffer): Unit

    open suspend fun write(b: Int) = write(byteArrayOf(b.toByte()))
    open suspend fun write(b: ByteArray): Unit = write(b, 0, b.size)
    open suspend fun write(b: ByteArray, off: Int, len: Int):Unit = write(ByteBuffer.wrap(b, off, len))

    open suspend fun flush() {}
    override suspend fun close() {}
}