package org.ivcode.knio.tcp.utils

import org.ivcode.knio.ChannelHandler
import org.ivcode.knio.EventLoop
import org.ivcode.knio.tcp.TcpClientEventHandlerClear
import org.ivcode.knio.tcp.TcpClientEventHandlerSSL
import java.net.SocketAddress
import java.nio.channels.SocketChannel
import javax.net.ssl.SSLContext

/**
 * Registers a TCP client with the given address and handler.
 *
 * @param address The address to connect to.
 * @param handler The handler to manage the channel events.
 * @return An AutoCloseable instance to manage the lifecycle of the connection.
 */
fun EventLoop.registerTcpClient(address: SocketAddress, handler: ChannelHandler): AutoCloseable {
    val channel = SocketChannel.open()
    channel.configureBlocking(false)
    channel.connect(address)

    return TcpClientEventHandlerClear(handler).apply {
        register(channel, this)
    }
}

/**
 * Registers a TCP client with SSL with the given address, SSL context, and handler.
 *
 * @param address The address to connect to.
 * @param sslContext The SSL context to use for the connection.
 * @param handler The handler to manage the channel events.
 * @return An AutoCloseable instance to manage the lifecycle of the connection.
 */
fun EventLoop.registerTcpClientSSL(address: SocketAddress, sslContext: SSLContext, handler: ChannelHandler): AutoCloseable {
    val channel = SocketChannel.open()
    channel.configureBlocking(false)
    channel.connect(address)

    return TcpClientEventHandlerSSL(handler, true, sslContext).apply {
        register(channel, this)
    }
}