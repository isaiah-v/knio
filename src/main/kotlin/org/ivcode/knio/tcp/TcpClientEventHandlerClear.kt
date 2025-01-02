package org.ivcode.knio.tcp

import org.ivcode.knio.*
import org.ivcode.knio.utils.read
import java.nio.ByteBuffer

class TcpClientEventHandlerClear(
    private val handler: ChannelHandler
): TcpClientEventHandlerAbstract() {

    private lateinit var readBuffer: BufferCloseable
    private lateinit var writeBuffer: BufferCloseable

    private val tcpChannel: Channel = object : Channel {
        override val localAddress get() = this@TcpClientEventHandlerClear.localAddress
        override val remoteAddress get() = this@TcpClientEventHandlerClear.remoteAddress

        override fun write(data: ByteBuffer) = this@TcpClientEventHandlerClear.write(data)
        override fun reader(): Reader = this@TcpClientEventHandlerClear.getReader()
        override fun close() = this@TcpClientEventHandlerClear.close()
        override fun isOpen() = this@TcpClientEventHandlerClear.isOpen()
        override fun isReadable() = this@TcpClientEventHandlerClear.key.isReadable
        override fun isWritable() = this@TcpClientEventHandlerClear.key.isWritable
        override fun setInterestOps(ops: Int) = this@TcpClientEventHandlerClear.setInterestOps(ops)
        override fun getInterestOps(): Int = this@TcpClientEventHandlerClear.key.interestOps
    }

    override fun doConnect() {
        readBuffer = BufferPooled(1024)
        writeBuffer = BufferPooled(1024)

        handler.onConnected(tcpChannel)
    }

    override fun doRead() = handler.onRead()
    override fun doWrite() = handler.onWrite()

    private fun getReader(): Reader {
        return object : Reader {
            override fun read(): Boolean {
                return readNetwork() > 0
            }

            override val buffer: Buffer
                get() = readBuffer
        }
    }

    /**
     * Reads data from the network into the network buffer.
     *
     * @return The number of bytes read from the network.
     */
    private fun readNetwork(): Int {
        val bytesRead = channel.read(readBuffer)
        if (bytesRead == -1) {
            // Client disconnected
            close()
        }

        readBuffer.flip()
        return bytesRead
    }

    override fun close() {
        if (channel.isOpen) {
            super.close()
            readBuffer.close()
            writeBuffer.close()
            handler.onClosed()
        }
    }

    /**
     * Writes data to the channel.
     *
     * @param data The data to be written to the channel.
     */
    private fun write(data: ByteBuffer) {
        while (data.hasRemaining()) {
            channel.write(data)
        }
    }
}