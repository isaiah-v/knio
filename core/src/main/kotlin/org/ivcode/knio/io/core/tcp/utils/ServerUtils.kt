package org.ivcode.org.ivcode.knio.io.core.tcp.utils

import org.ivcode.knio.core.ChannelHandler
import org.ivcode.knio.core.EventHandler
import org.ivcode.knio.core.EventLoop
import org.ivcode.knio.core.tcp.TcpClientEventHandlerClear
import org.ivcode.knio.core.tcp.TcpClientEventHandlerSSL
import org.ivcode.knio.core.tcp.TcpServerEventHandler
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import javax.net.ssl.SSLContext

/**
 * Registers a TCP server on the specified port with the given handler provider.
 *
 * @param port The port to bind the server to.
 * @param handlerProvider A function that provides a NioChannelHandler for each client connection.
 * @return An AutoCloseable instance to manage the lifecycle of the server.
 */
fun EventLoop.registerTcpServer(port: Int, handlerProvider: () -> ChannelHandler): AutoCloseable {
    val serverChannel = ServerSocketChannel.open()
    serverChannel.configureBlocking(false)
    serverChannel.bind(InetSocketAddress(port))

    val clearClientProvider: () -> EventHandler = {
        val handler = handlerProvider()
        TcpClientEventHandlerClear(handler)
    }

    return TcpServerEventHandler(clearClientProvider).apply {
        register(serverChannel, this, SelectionKey.OP_ACCEPT)
    }
}

/**
 * Registers a TCP server with SSL on the specified port with the given handler provider.
 *
 * @param port The port to bind the server to.
 * @param sslContext The SSL context to use for secure connections.
 * @param handlerProvider A function that provides a NioChannelHandler for each client connection.
 * @return An AutoCloseable instance to manage the lifecycle of the server.
 */
fun EventLoop.registerTcpServerSSL(port: Int, sslContext: SSLContext, handlerProvider: () -> ChannelHandler): AutoCloseable {
    val serverChannel = ServerSocketChannel.open()
    serverChannel.configureBlocking(false)
    serverChannel.bind(InetSocketAddress(port))

    val sslClientProvider: () -> EventHandler = {
        val handler = handlerProvider()
        TcpClientEventHandlerSSL(handler, false, sslContext)
    }

    return TcpServerEventHandler(sslClientProvider).apply {
        register(serverChannel, this, SelectionKey.OP_ACCEPT)
    }
}