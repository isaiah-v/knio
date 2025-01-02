package org.ivcode.knio

import java.nio.ByteBuffer

abstract class BufferAbstract: Buffer {

    override fun position(): Int = buffer().position()
    override fun position(newPosition: Int): Buffer = apply { buffer().position(newPosition) }
    override fun limit(): Int = buffer().limit()
    override fun limit(newLimit: Int): Buffer = apply { buffer().limit(newLimit) }
    override fun capacity(): Int = buffer().capacity()
    override fun clear(): Buffer = apply { buffer().clear() }
    override fun flip(): Buffer = apply { buffer().flip() }
    override fun rewind(): Buffer = apply { buffer().rewind() }
    override fun compact(): Buffer = apply { buffer().compact() }
    override fun get(): Byte = buffer().get()
    override fun get(dst: ByteArray): Buffer = apply { buffer().get(dst) }
    override fun get(dst: ByteArray, offset: Int, length: Int): Buffer = apply { buffer().get(dst, offset, length) }
    override fun get(index: Int): Byte = buffer().get(index)
    override fun put(b: Byte): Buffer = apply { buffer().put(b) }
    override fun put(src: ByteArray): Buffer = apply { buffer().put(src) }
    override fun put(src: ByteArray, offset: Int, length: Int): Buffer = apply { buffer().put(src, offset, length) }
    override fun put(index: Int, b: Byte): Buffer = apply { buffer().put(index, b) }
    override fun getChar(): Char = buffer().getChar()
    override fun getChar(index: Int): Char = buffer().getChar(index)
    override fun putChar(c: Char): Buffer = apply { buffer().putChar(c) }
    override fun putChar(index: Int, c: Char): Buffer = apply { buffer().putChar(index, c) }

    override fun getShort(): Short = buffer().getShort()
    override fun getShort(index: Int): Short = buffer().getShort(index)
    override fun putShort(s: Short): Buffer = apply { buffer().putShort(s) }
    override fun putShort(index: Int, s: Short): Buffer = apply { buffer().putShort(index, s) }

    override fun getInt(): Int = buffer().getInt()
    override fun getInt(index: Int): Int = buffer().getInt(index)
    override fun putInt(i: Int): Buffer = apply { buffer().putInt(i) }
    override fun putInt(index: Int, i: Int): Buffer = apply { buffer().putInt(index, i) }

    override fun getLong(): Long = buffer().getLong()
    override fun getLong(index: Int): Long = buffer().getLong(index)
    override fun putLong(l: Long): Buffer = apply { buffer().putLong(l) }
    override fun putLong(index: Int, l: Long): Buffer = apply { buffer().putLong(index, l) }

    override fun getFloat(): Float = buffer().getFloat()
    override fun getFloat(index: Int): Float = buffer().getFloat(index)
    override fun putFloat(f: Float): Buffer = apply { buffer().putFloat(f) }
    override fun putFloat(index: Int, f: Float): Buffer = apply { buffer().putFloat(index, f) }

    override fun getDouble(): Double = buffer().getDouble()
    override fun getDouble(index: Int): Double = buffer().getDouble(index)
    override fun putDouble(d: Double): Buffer = apply { buffer().putDouble(d) }
    override fun putDouble(index: Int, d: Double): Buffer = apply { buffer().putDouble(index, d) }

    override fun hasRemaining(): Boolean = buffer().hasRemaining()
    override fun remaining(): Int = buffer().remaining()

    override fun slice(): Buffer = NioByteBufferAbstractWrapper(buffer().slice())
    override fun duplicate(): Buffer = NioByteBufferAbstractWrapper(buffer().duplicate())

    override fun isDirect(): Boolean = buffer().isDirect
    override fun isReadOnly(): Boolean = buffer().isReadOnly

    // Wrapper class for NioByteBuffer to delegate buffer methods
    private class NioByteBufferAbstractWrapper(private val delegate: ByteBuffer) : Buffer {
        override fun buffer(): ByteBuffer = delegate

        override fun position(): Int = delegate.position()
        override fun position(newPosition: Int): Buffer = apply { delegate.position(newPosition) }
        override fun limit(): Int = delegate.limit()
        override fun limit(newLimit: Int): Buffer = apply { delegate.limit(newLimit) }
        override fun capacity(): Int = delegate.capacity()
        override fun clear(): Buffer = apply { delegate.clear() }
        override fun flip(): Buffer = apply { delegate.flip() }
        override fun rewind(): Buffer = apply { delegate.rewind() }
        override fun compact(): Buffer = apply { delegate.compact() }

        override fun get(): Byte = delegate.get()
        override fun get(dst: ByteArray): Buffer = apply { delegate.get(dst) }
        override fun get(dst: ByteArray, offset: Int, length: Int): Buffer = apply { delegate.get(dst, offset, length) }
        override fun get(index: Int): Byte = delegate.get(index)
        override fun put(b: Byte): Buffer = apply { delegate.put(b) }
        override fun put(src: ByteArray): Buffer = apply { delegate.put(src) }
        override fun put(src: ByteArray, offset: Int, length: Int): Buffer = apply { delegate.put(src, offset, length) }
        override fun put(index: Int, b: Byte): Buffer = apply { delegate.put(index, b) }
        override fun getChar(): Char = delegate.getChar()
        override fun getChar(index: Int): Char = delegate.getChar(index)
        override fun putChar(c: Char): Buffer = apply { delegate.putChar(c) }
        override fun putChar(index: Int, c: Char): Buffer = apply { delegate.putChar(index, c) }

        override fun getShort(): Short = delegate.getShort()
        override fun getShort(index: Int): Short = delegate.getShort(index)
        override fun putShort(s: Short): Buffer = apply { delegate.putShort(s) }
        override fun putShort(index: Int, s: Short): Buffer = apply { delegate.putShort(index, s) }

        override fun getInt(): Int = delegate.getInt()
        override fun getInt(index: Int): Int = delegate.getInt(index)
        override fun putInt(i: Int): Buffer = apply { delegate.putInt(i) }
        override fun putInt(index: Int, i: Int): Buffer = apply { delegate.putInt(index, i) }

        override fun getLong(): Long = delegate.getLong()
        override fun getLong(index: Int): Long = delegate.getLong(index)
        override fun putLong(l: Long): Buffer = apply { delegate.putLong(l) }
        override fun putLong(index: Int, l: Long): Buffer = apply { delegate.putLong(index, l) }

        override fun getFloat(): Float = delegate.getFloat()
        override fun getFloat(index: Int): Float = delegate.getFloat(index)
        override fun putFloat(f: Float): Buffer = apply { delegate.putFloat(f) }
        override fun putFloat(index: Int, f: Float): Buffer = apply { delegate.putFloat(index, f) }

        override fun getDouble(): Double = delegate.getDouble()
        override fun getDouble(index: Int): Double = delegate.getDouble(index)
        override fun putDouble(d: Double): Buffer = apply { delegate.putDouble(d) }
        override fun putDouble(index: Int, d: Double): Buffer = apply { delegate.putDouble(index, d) }

        override fun hasRemaining(): Boolean = delegate.hasRemaining()
        override fun remaining(): Int = delegate.remaining()

        override fun slice(): Buffer = NioByteBufferAbstractWrapper(delegate.slice())
        override fun duplicate(): Buffer = NioByteBufferAbstractWrapper(delegate.duplicate())

        override fun isDirect(): Boolean = delegate.isDirect
        override fun isReadOnly(): Boolean = delegate.isReadOnly
    }
}