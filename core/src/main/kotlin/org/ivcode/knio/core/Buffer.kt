package org.ivcode.knio.core

import java.nio.*

/**
 * A [ByteBuffer] wrapping allowing for backend-agnostic operations. This includes resizing and pooling.
 */
interface Buffer {
    /**
     * Returns the underlying buffer.
     *
     * This method is only for compatibility. The underlying buffer may be changed out.
     * It's safe to use this method for operations but don't hold a reference to the buffer.
     *
     * @return the underlying ByteBuffer
     */
    fun buffer(): ByteBuffer

    // Buffer position and limit manipulation methods

    /**
     * Returns the current position of the buffer.
     *
     * @return the current position
     */
    fun position(): Int

    /**
     * Sets the buffer's position.
     *
     * @param newPosition the new position
     * @return the NioByteBuffer instance
     */
    fun position(newPosition: Int): Buffer

    /**
     * Returns the buffer's limit.
     *
     * @return the current limit
     */
    fun limit(): Int

    /**
     * Sets the buffer's limit.
     *
     * @param newLimit the new limit
     * @return the NioByteBuffer instance
     */
    fun limit(newLimit: Int): Buffer

    /**
     * Returns the buffer's capacity.
     *
     * @return the capacity
     */
    fun capacity(): Int

    // Buffer flip and clear methods

    /**
     * Clears the buffer. The position is set to zero, the limit is set to the capacity, and the mark is discarded.
     *
     * @return the NioByteBuffer instance
     */
    fun clear(): Buffer

    /**
     * Flips the buffer. The limit is set to the current position and then the position is set to zero.
     *
     * @return the NioByteBuffer instance
     */
    fun flip(): Buffer

    /**
     * Rewinds the buffer. The position is set to zero and the mark is discarded.
     *
     * @return the NioByteBuffer instance
     */
    fun rewind(): Buffer

    /**
     * Compacts the buffer. The bytes between the buffer's current position and its limit are copied to the beginning of the buffer.
     *
     * @return the NioByteBuffer instance
     */
    fun compact(): Buffer

    // Get and put methods (for reading and writing data)

    /**
     * Reads the byte at the buffer's current position.
     *
     * @return the byte at the current position
     */
    fun get(): Byte

    /**
     * Transfers bytes from this buffer into the given destination array.
     *
     * @param dst the destination array
     * @return the NioByteBuffer instance
     */
    fun get(dst: ByteArray): Buffer

    /**
     * Transfers bytes from this buffer into the given destination array.
     *
     * @param dst the destination array
     * @param offset the offset within the array of the first byte to be written
     * @param length the maximum number of bytes to be written to the given array
     * @return the NioByteBuffer instance
     */
    fun get(dst: ByteArray, offset: Int, length: Int): Buffer

    /**
     * Writes the given byte into this buffer at the current position.
     *
     * @param b the byte to be written
     * @return the NioByteBuffer instance
     */
    fun put(b: Byte): Buffer

    /**
     * Writes bytes from the given source array into this buffer.
     *
     * @param src the source array
     * @return the NioByteBuffer instance
     */
    fun put(src: ByteArray): Buffer

    /**
     * Writes bytes from the given source array into this buffer.
     *
     * @param src the source array
     * @param offset the offset within the array of the first byte to be read
     * @param length the number of bytes to be read from the given array
     * @return the NioByteBuffer instance
     */
    fun put(src: ByteArray, offset: Int, length: Int): Buffer

    /**
     * Reads the byte at the given index.
     *
     * @param index the index from which the byte will be read
     * @return the byte at the given index
     */
    fun get(index: Int): Byte

    /**
     * Writes the given byte into this buffer at the given index.
     *
     * @param index the index at which the byte will be written
     * @param b the byte to be written
     * @return the NioByteBuffer instance
     */
    fun put(index: Int, b: Byte): Buffer

    // Data access methods (for various primitive types)

    /**
     * Reads the next two bytes at this buffer's current position, composing them into a char value.
     *
     * @return the char value at the current position
     */
    fun getChar(): Char

    /**
     * Reads two bytes at the given index, composing them into a char value.
     *
     * @param index the index from which the bytes will be read
     * @return the char value at the given index
     */
    fun getChar(index: Int): Char

    /**
     * Writes two bytes containing the given char value into this buffer at the current position.
     *
     * @param c the char value to be written
     * @return the NioByteBuffer instance
     */
    fun putChar(c: Char): Buffer

    /**
     * Writes two bytes containing the given char value into this buffer at the given index.
     *
     * @param index the index at which the bytes will be written
     * @param c the char value to be written
     * @return the NioByteBuffer instance
     */
    fun putChar(index: Int, c: Char): Buffer

    /**
     * Reads the next two bytes at this buffer's current position, composing them into a short value.
     *
     * @return the short value at the current position
     */
    fun getShort(): Short

    /**
     * Reads two bytes at the given index, composing them into a short value.
     *
     * @param index the index from which the bytes will be read
     * @return the short value at the given index
     */
    fun getShort(index: Int): Short

    /**
     * Writes two bytes containing the given short value into this buffer at the current position.
     *
     * @param s the short value to be written
     * @return the NioByteBuffer instance
     */
    fun putShort(s: Short): Buffer

    /**
     * Writes two bytes containing the given short value into this buffer at the given index.
     *
     * @param index the index at which the bytes will be written
     * @param s the short value to be written
     * @return the NioByteBuffer instance
     */
    fun putShort(index: Int, s: Short): Buffer

    /**
     * Reads the next four bytes at this buffer's current position, composing them into an int value.
     *
     * @return the int value at the current position
     */
    fun getInt(): Int

    /**
     * Reads four bytes at the given index, composing them into an int value.
     *
     * @param index the index from which the bytes will be read
     * @return the int value at the given index
     */
    fun getInt(index: Int): Int

    /**
     * Writes four bytes containing the given int value into this buffer at the current position.
     *
     * @param i the int value to be written
     * @return the NioByteBuffer instance
     */
    fun putInt(i: Int): Buffer

    /**
     * Writes four bytes containing the given int value into this buffer at the given index.
     *
     * @param index the index at which the bytes will be written
     * @param i the int value to be written
     * @return the NioByteBuffer instance
     */
    fun putInt(index: Int, i: Int): Buffer

    /**
     * Reads the next eight bytes at this buffer's current position, composing them into a long value.
     *
     * @return the long value at the current position
     */
    fun getLong(): Long

    /**
     * Reads eight bytes at the given index, composing them into a long value.
     *
     * @param index the index from which the bytes will be read
     * @return the long value at the given index
     */
    fun getLong(index: Int): Long

    /**
     * Writes eight bytes containing the given long value into this buffer at the current position.
     *
     * @param l the long value to be written
     * @return the NioByteBuffer instance
     */
    fun putLong(l: Long): Buffer

    /**
     * Writes eight bytes containing the given long value into this buffer at the given index.
     *
     * @param index the index at which the bytes will be written
     * @param l the long value to be written
     * @return the NioByteBuffer instance
     */
    fun putLong(index: Int, l: Long): Buffer

    /**
     * Reads the next four bytes at this buffer's current position, composing them into a float value.
     *
     * @return the float value at the current position
     */
    fun getFloat(): Float

    /**
     * Reads four bytes at the given index, composing them into a float value.
     *
     * @param index the index from which the bytes will be read
     * @return the float value at the given index
     */
    fun getFloat(index: Int): Float

    /**
     * Writes four bytes containing the given float value into this buffer at the current position.
     *
     * @param f the float value to be written
     * @return the NioByteBuffer instance
     */
    fun putFloat(f: Float): Buffer

    /**
     * Writes four bytes containing the given float value into this buffer at the given index.
     *
     * @param index the index at which the bytes will be written
     * @param f the float value to be written
     * @return the NioByteBuffer instance
     */
    fun putFloat(index: Int, f: Float): Buffer

    /**
     * Reads the next eight bytes at this buffer's current position, composing them into a double value.
     *
     * @return the double value at the current position
     */
    fun getDouble(): Double

    /**
     * Reads eight bytes at the given index, composing them into a double value.
     *
     * @param index the index from which the bytes will be read
     * @return the double value at the given index
     */
    fun getDouble(index: Int): Double

    /**
     * Writes eight bytes containing the given double value into this buffer at the current position.
     *
     * @param d the double value to be written
     * @return the NioByteBuffer instance
     */
    fun putDouble(d: Double): Buffer

    /**
     * Writes eight bytes containing the given double value into this buffer at the given index.
     *
     * @param index the index at which the bytes will be written
     * @param d the double value to be written
     * @return the NioByteBuffer instance
     */
    fun putDouble(index: Int, d: Double): Buffer

    // Other utility methods

    /**
     * Tells whether there are any elements between the current position and the limit.
     *
     * @return true if there are elements remaining, false otherwise
     */
    fun hasRemaining(): Boolean

    /**
     * Returns the number of elements between the current position and the limit.
     *
     * @return the number of remaining elements
     */
    fun remaining(): Int

    /**
     * Creates a new buffer that shares this buffer's content.
     *
     * @return the new buffer
     */
    fun slice(): Buffer

    /**
     * Creates a new buffer that shares this buffer's content.
     *
     * @return the new buffer
     */
    fun duplicate(): Buffer

    /**
     * Tells whether or not this buffer is direct.
     *
     * @return true if this buffer is direct, false otherwise
     */
    fun isDirect(): Boolean

    /**
     * Tells whether or not this buffer is read-only.
     *
     * @return true if this buffer is read-only, false otherwise
     */
    fun isReadOnly(): Boolean
}