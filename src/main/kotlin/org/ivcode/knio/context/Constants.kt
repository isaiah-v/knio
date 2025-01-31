package org.ivcode.knio.context

internal const val BYTES_PER_BYTE   = 1
internal const val BYTES_PER_CHAR   = 2
internal const val BYTES_PER_SHORT  = 2
internal const val BYTES_PER_INT    = 4
internal const val BYTES_PER_LONG   = 8
internal const val BYTES_PER_FLOAT  = 4
internal const val BYTES_PER_DOUBLE = 8


internal const val DEFAULT_TASK_BUFFER_SIZE = 1024
internal const val DEFAULT_STREAM_BUFFER_SIZE = 8 * 1024

/**
 * Gets the size of a buffer for a given unit type (byte, char, int,... etc.)
 */
internal fun getBufferSize(size: Int, bytesPerUnit: Int): Int {
    if(size < 0) {
        throw IllegalArgumentException("size must be greater than or equal to 0")
    }
    val unitSize = size * bytesPerUnit
    if (unitSize < 0) {
        throw IllegalArgumentException("size is too large")
    }
    return unitSize
}

internal fun getCharBufferSize(size: Int): Int = getBufferSize(size, BYTES_PER_CHAR)
internal fun getShortBufferSize(size: Int): Int = getBufferSize(size, BYTES_PER_SHORT)
internal fun getIntBufferSize(size: Int): Int = getBufferSize(size, BYTES_PER_INT)
internal fun getLongBufferSize(size: Int): Int = getBufferSize(size, BYTES_PER_LONG)
internal fun getFloatBufferSize(size: Int): Int = getBufferSize(size, BYTES_PER_FLOAT)
internal fun getDoubleBufferSize(size: Int): Int = getBufferSize(size, BYTES_PER_DOUBLE)