package org.knio.core.utils

import java.nio.ByteBuffer


/**
 * Transfers as many bytes as possible from this buffer to the destination buffer.
 *
 * If this buffer is smaller than the destination buffer, the remaining bytes in
 * this buffer are transferred. If the destination buffer is smaller, the number
 * of bytes that can be transferred are transferred.
 *
 * The position of both buffers are updated to reflect the transfer.
 *
 * @throws java.nio.ReadOnlyBufferException if the destination buffer is read-only
 */
internal fun ByteBuffer.transferTo(dest: ByteBuffer): Int {
    val read = minOf(this.remaining(), dest.remaining())
    if(read == 0) return -1

    dest.put(dest.position(), this, this.position(), read)

    this.position(this.position() + read)
    dest.position(dest.position() + read)

    return read
}