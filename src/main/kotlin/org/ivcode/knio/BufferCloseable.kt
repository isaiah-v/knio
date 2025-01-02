package org.ivcode.knio

interface BufferCloseable: Buffer, AutoCloseable {
    override fun close()
}