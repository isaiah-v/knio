package org.ivcode.org.ivcode.knio.io.core.tcp

import org.ivcode.knio.core.ClosedSocketException
import org.ivcode.knio.core.EventHandler
import org.ivcode.knio.core.EventKey
import org.ivcode.knio.core.Channel.Companion.filterOps
import java.net.SocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

abstract class TcpClientEventHandlerAbstract: EventHandler {

    protected lateinit var key: EventKey

    /**
     * The socket channel associated with this TCP connection.
     */
    protected val channel: SocketChannel get() = key.channel as SocketChannel

    /**
     * The local address of the socket channel.
     */
    protected lateinit var localAddress: SocketAddress

    /**
     * The remote address of the socket channel.
     */
    protected lateinit var remoteAddress: SocketAddress


    final override fun onRegister(key: EventKey) {
        this.key = key

        if(!channel.isConnectionPending) {
            onConnect()
        } else {
            key.interestOps = SelectionKey.OP_CONNECT
        }
    }

    private fun onConnect() {
        if (channel.isConnectionPending) {
            channel.finishConnect()
            println("Connection finished")
        }

        this.localAddress = channel.localAddress
        this.remoteAddress = channel.remoteAddress

        doConnect()
    }

    final override fun onEvent() {
        try {
            if (key.isValid && key.isConnectable) {
                onConnect()
            }
            if (key.isValid && key.isReadable) {
                eventFilter(this::doRead)
            }
            if (key.isValid && key.isWritable) {
                eventFilter(this::doWrite)
            }
        } catch (e: ClosedSocketException) {
            close()
        }
    }

    protected open fun eventFilter(event: () -> Unit) {
        event()
    }

    protected fun setInterestOps(ops: Int) {
        key.interestOps = filterOps(ops)
    }

    fun isOpen(): Boolean = channel.isOpen

    override fun close() {
        key.cancel()
        channel.close()
    }

    abstract fun doConnect()
    abstract fun doRead()
    abstract fun doWrite()
}
