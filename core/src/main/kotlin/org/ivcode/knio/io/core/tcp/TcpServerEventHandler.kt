package org.ivcode.org.ivcode.knio.io.core.tcp

import org.ivcode.knio.core.EventHandler
import org.ivcode.knio.core.EventKey
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

class TcpServerEventHandler(
    private val clientEventHandlerFactory: () -> EventHandler
): EventHandler {

    private lateinit var key: EventKey

    override fun onRegister(key: EventKey) {
        this.key = key
        key.interestOps = SelectionKey.OP_ACCEPT
    }

    override fun onEvent() {
        if(key.isValid && key.isAcceptable) {
            doAccept()
        }
    }

    private fun doAccept() {
        val serverChannel = key.channel as ServerSocketChannel
        val clientChannel = serverChannel.accept()

        if(clientChannel.isConnectionPending) {
            println("connection pending")
        }

        key.eventLoop.register(clientChannel, clientEventHandlerFactory())
    }

    override fun close() {
        key.cancel()
    }
}
