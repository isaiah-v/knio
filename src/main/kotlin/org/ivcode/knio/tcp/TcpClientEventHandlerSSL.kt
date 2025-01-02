package org.ivcode.knio.tcp

import org.ivcode.knio.Reader
import org.ivcode.knio.SSLManager
import org.ivcode.knio.Channel
import org.ivcode.knio.ChannelHandler
import java.nio.ByteBuffer
import javax.net.ssl.SSLContext

class TcpClientEventHandlerSSL(
    private val tcpHandler: ChannelHandler,
    useClientMode: Boolean,
    sslContext: SSLContext,
): TcpClientEventHandlerAbstract() {

    private val sslManager: SSLManager = SSLManager(useClientMode, sslContext)

    private val tcpChannel: Channel = object : Channel {
        override val localAddress get() = this@TcpClientEventHandlerSSL.localAddress
        override val remoteAddress get() = this@TcpClientEventHandlerSSL.remoteAddress

        override fun write(data: ByteBuffer) = this@TcpClientEventHandlerSSL.write(data)
        override fun reader(): Reader = sslManager.getReader(key)
        override fun close() = this@TcpClientEventHandlerSSL.close()
        override fun isOpen() = this@TcpClientEventHandlerSSL.isOpen()
        override fun isReadable() = this@TcpClientEventHandlerSSL.key.isReadable
        override fun isWritable() = this@TcpClientEventHandlerSSL.key.isWritable
        override fun setInterestOps(ops: Int) = this@TcpClientEventHandlerSSL.setInterestOps(ops)
        override fun getInterestOps(): Int = this@TcpClientEventHandlerSSL.key.interestOps
    }

    override fun doConnect() {
        sslManager.beginHandshake(key)
    }


    override fun eventFilter(event: ()->Unit) {
        if(sslManager.handshakeEvent(key, this::onHandshakeComplete)) {
            return
        }  else {
            event()
        }
    }

    fun onHandshakeComplete() {
        tcpHandler.onConnected(tcpChannel)
    }

    /**
     * Handles reading data from the connection.
     *
     * @param key The selection key representing the connection.
     */
    override fun doRead() = tcpHandler.onRead()

    override fun doWrite() = tcpHandler.onWrite()

    /**
     * Writes data to the connection.
     *
     * @param data The data to be written.
     */
    private fun write(data: ByteBuffer) {
        sslManager.write(key, data)
    }

    /**
     * Closes the connection.
     */
    override fun close() {
        if (!channel.isOpen) {
            return
        }

        try {
            sslManager.close(key)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            super.close()
            tcpHandler.onClosed()
        }
    }
}