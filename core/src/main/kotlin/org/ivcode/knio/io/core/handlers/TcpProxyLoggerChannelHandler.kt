package org.ivcode.org.ivcode.knio.io.core.handlers

import org.ivcode.knio.core.Channel
import org.ivcode.knio.core.ChannelHandler
import org.ivcode.knio.core.EventLoop
import org.ivcode.knio.core.tcp.utils.registerTcpClient
import org.ivcode.knio.core.tcp.utils.registerTcpClientSSL
import org.ivcode.knio.core.utils.addWriteInterest
import org.ivcode.knio.core.utils.removeWriteInterest
import java.io.ByteArrayOutputStream
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.SSLContext

/**
 * A NioChannelHandler that acts as a proxy for a TCP connection and logs all data that passes through it.
 *
 * @param address The address of the proxy server.
 * @param charset The charset to use when logging data as strings.
 * @param eventLoop The event loop to use when connecting to the proxy.
 * @param sslContext The SSL context to use for secure connections, or null for non-secure connections.
 */
class TcpProxyLoggerChannelHandler (
    private val address: SocketAddress,
    private val charset: Charset = Charsets.UTF_8,
    private val eventLoop: EventLoop,
    private val sslContext: SSLContext? = null
): ChannelHandler {

    companion object {
        private var ID_COUNTER = AtomicLong(0)
    }

    /** The unique identifier for this connection */
    private val id = ID_COUNTER.getAndIncrement()

    /** The channel that the client is connected to */
    private lateinit var clientChannel: Channel

    /** The channel that we're proxying to */
    private var proxyChannel: Channel? = null

    /** The buffer that the client writes to. */
    private val clientBuffer = ByteArrayOutputStream()

    /** The buffer that the proxy writes to. */
    private val proxyBuffer = ByteArrayOutputStream()

    /**
     * Called when the client channel is connected.
     *
     * @param channel The client channel that is connected.
     */
    override fun onConnected(channel: Channel) {
        this.clientChannel = channel
        clientChannel.setInterestOps(Channel.LISTEN_READ)

        // the handler for the proxy channel (all methods are implemented in this class)
        val proxyHandler: ChannelHandler = object : ChannelHandler {
            override fun onConnected(channel: Channel) = proxyConnected(channel)
            override fun onRead() = proxyRead()
            override fun onWrite() = proxyWrite()
            override fun onClosed() = proxyClosed()
        }

        // connect to the proxy
        if(sslContext != null) {
            eventLoop.registerTcpClientSSL(address, sslContext, proxyHandler)
        } else {
            eventLoop.registerTcpClient(address, proxyHandler)
        }
    }

    /**
     * Called when there is data to read from the client channel.
     */
    override fun onRead() {
        if(read(false)) {
            // tell the proxy there is data to write
            val channel = proxyChannel
            if(channel!=null && channel.isOpen()) {
                channel.addWriteInterest()
            }
        }
    }

    /**
     * Called when the client channel is ready to write data.
     */
    override fun onWrite() {
        write(false)

        val channel = proxyChannel
        if(channel!=null && !channel.isOpen()) {
            // If the other channel closed, close this channel when done writing
            clientChannel.close()
        }
    }

    /**
     * Called when the client channel is closed.
     */
    override fun onClosed() {
        // close the other channel
        if(clientBuffer.size() == 0) {
            proxyChannel?.close()
        }
    }

    /**
     * Called when the proxy channel is connected.
     *
     * @param proxy The proxy channel that is connected.
     */
    private fun proxyConnected(proxy: Channel) {
        this.proxyChannel = proxy
        proxy.setInterestOps(Channel.LISTEN_READ)

        // if data had been read from the client, tell the proxy there is data to write
        if(clientBuffer.size() > 0) {
            proxy.addWriteInterest()
        }
    }

    /**
     * Called when there is data to read from the proxy channel.
     */
    private fun proxyRead() {
        if(read(true)) {
            // tell the client there is data to write
            clientChannel.addWriteInterest()
        }
    }

    /**
     * Called when the proxy channel is ready to write data.
     */
    private fun proxyWrite() {
        write(true)

        if(!clientChannel.isOpen()) {
            // If the other channel closed, close this channel when done writing
            proxyChannel?.close()
        }
    }

    /**
     * Called when the proxy channel is closed.
     */
    private fun proxyClosed() {
        // close the other channel if there is no data to write
        if(proxyBuffer.size() == 0) {
            clientChannel.close()
        }
    }

    /**
     * Reads data from the specified channel.
     *
     * @param isProxy True if reading from the proxy channel, false if reading from the client channel.
     * @return True if data was read, false otherwise.
     */
    private fun read(isProxy: Boolean): Boolean {
        val channel = if(isProxy) proxyChannel!! else clientChannel
        val buffer = if(isProxy) proxyBuffer else clientBuffer

        val direction = if(isProxy) "<--" else "-->"

        var isReadData = false

        val reader = channel.reader()
        while (reader.read()) {
            val size = reader.buffer.remaining()
            if(size == 0) {
                continue
            }
            if(!isReadData) {
                println("# --== START: $id ==-- #")
                println("# --== ${clientChannel.localAddress} $direction $address ==-- #")
                isReadData = true
            }

            val bytes = ByteArray(size)
            reader.buffer.get(bytes)
            buffer.write(bytes)

            println(String(bytes, charset))

            reader.buffer.clear()
        }

        if(isReadData) {
            println("# --== END: $id ==-- #")
        }

        return isReadData
    }

    /**
     * Writes data to the specified channel.
     *
     * @param isProxy True if writing to the proxy channel, false if writing to the client channel.
     */
    private fun write(isProxy: Boolean) {
        val channel = if(isProxy) proxyChannel!! else clientChannel
        val buffer = if(isProxy) clientBuffer else proxyBuffer

        val writeData = buffer.toByteArray()
        val writeBuffer = ByteBuffer.wrap(writeData)

        while (writeBuffer.hasRemaining()) {
            channel.write(writeBuffer)
        }
        buffer.reset()

        // remove write interest if there is no more data to write
        channel.removeWriteInterest()
    }
}