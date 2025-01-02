package org.ivcode.knio

interface BufferResizable: Buffer {
    fun resize(capacity: Int)
}