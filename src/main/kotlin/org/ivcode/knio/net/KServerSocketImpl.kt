package org.ivcode.knio.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ivcode.knio.utils.asCompletionHandler
import org.ivcode.knio.utils.timeout
import java.net.*
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import kotlin.coroutines.suspendCoroutine

internal class KServerSocketImpl(
    private val channel: AsynchronousServerSocketChannel
): KServerSocket {
    private val acceptMutex = Mutex()

    private var acceptTimeout: Long? = null

    override suspend fun accept(): KSocket {
        acceptMutex.withLock {
            return accept0()
        }
    }

    private suspend fun accept0(): KSocket = suspendCoroutine { continuation ->
        val transformer = {
                r: AsynchronousSocketChannel -> KSocketImpl(r)
        }

        val timeout = acceptTimeout
        val handler = if(timeout != null) {
            val timeoutJob = continuation.timeout(timeout) { SocketTimeoutException() }
            transformer.asCompletionHandler(timeoutJob = timeoutJob)
        } else {
            transformer.asCompletionHandler()
        }

        channel.accept(continuation, handler)
    }

    override suspend fun bind(endpoint: SocketAddress, backlog: Int): Unit = withContext(Dispatchers.IO) {
        channel.bind(endpoint, backlog)
    }

    override suspend fun close(): Unit = acceptMutex.withLock {
        channel.close()
        acceptTimeout = null
    }

    fun getChannel(): AsynchronousServerSocketChannel = channel

    override suspend fun getInetAddress(): InetAddress?  {
        val address = channel.localAddress ?: return null
        return if(address is InetSocketAddress) {
            address.address
        } else {
            null
        }
    }

    override suspend fun getLocalPort(): Int {
        val address = channel.localAddress ?: return -1
        return if(address is InetSocketAddress) {
            address.port
        } else {
            -1
        }
    }

    override suspend fun getLocalSocketAddress(): SocketAddress? = channel.localAddress
    override suspend fun getReceiveBufferSize(): Int = withContext(Dispatchers.IO) {
        channel.getOption(StandardSocketOptions.SO_RCVBUF)
    }
    override suspend fun getReuseAddress(): Boolean = withContext(Dispatchers.IO) {
        channel.getOption(StandardSocketOptions.SO_REUSEADDR)
    }
    override suspend fun getAcceptTimeout(): Long = acceptTimeout ?: 0
    override suspend fun isBound(): Boolean = channel.localAddress != null
    override suspend fun isClosed(): Boolean = !channel.isOpen
    override suspend fun setReceiveBufferSize(size: Int): Unit = withContext(Dispatchers.IO) {
        channel.setOption(StandardSocketOptions.SO_RCVBUF, size)
    }

    override suspend fun setReuseAddress(on: Boolean): Unit = withContext(Dispatchers.IO) {
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, on)
    }

    override suspend fun setAcceptTimeout(timeout: Long) {
        require(timeout >= 0) { "timeout value is negative" }
        this.acceptTimeout = timeout
    }
}