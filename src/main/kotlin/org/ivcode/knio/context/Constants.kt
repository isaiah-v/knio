package org.ivcode.knio.context

const val BYTES_PER_BYTE  = 1
const val BYTES_PER_CHAR  = 2
const val BYTES_PER_SHORT = 2
const val BYTES_PER_INT   = 4
const val BYTES_PER_LONG  = 8

const val DEFAULT_TASK_BUFFER_SIZE = 1024
const val DEFAULT_STREAM_BUFFER_SIZE = 8 * 1024

/**
 * Gets the size of a buffer for a given unit (byte, char, int,... etc).
 */
internal fun getUnitBufferSize(size: Int, bytesPerUnit: Int): Int {
    if(size < 0) {
        throw IllegalArgumentException("size must be greater than or equal to 0")
    }
    val unitSize = size * bytesPerUnit
    if (unitSize < 0) {
        throw IllegalArgumentException("size is too large")
    }
    return unitSize
}

internal fun getCharBufferSize(size: Int): Int = getUnitBufferSize(size, BYTES_PER_CHAR)
internal fun getShortBufferSize(size: Int): Int = getUnitBufferSize(size, BYTES_PER_SHORT)
internal fun getIntBufferSize(size: Int): Int = getUnitBufferSize(size, BYTES_PER_INT)
internal fun getLongBufferSize(size: Int): Int = getUnitBufferSize(size, BYTES_PER_LONG)