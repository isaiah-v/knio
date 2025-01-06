package org.ivcode.knio.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ivcode.knio.lang.KAutoCloseable
import org.ivcode.knio.utils.asCompletionHandler
import org.ivcode.knio.utils.timeout
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.SocketTimeoutException
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import kotlin.coroutines.suspendCoroutine

class KServerSocket: KAutoCloseable {
    companion object {
        suspend fun open(port: Int, backlog: Int=0, bindAddress: InetAddress? = null): KServerSocket {
            val serverSocket = KServerSocket()
            try {
                require(port in 1..0xfffe) { "Port value out of range: $port" }


                val bkLog = if (backlog < 1) 50 else backlog
                val local = InetSocketAddress(bindAddress, port)

                serverSocket.bind(local, bkLog)
            } catch  (th: Throwable) {
                serverSocket.close()
            }

            return serverSocket
        }
    }

    private val acceptMutex = Mutex()

    private val channel = AsynchronousServerSocketChannel.open()
    private var acceptTimeout: Long? = null

    suspend fun accept(): KSocket {
        acceptMutex.withLock {
            return accept0()
        }
    }

    private suspend fun accept0(): KSocket = suspendCoroutine { continuation ->
        val transformer = {
            r: AsynchronousSocketChannel -> KSocket(r)
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

    suspend fun bind(local: SocketAddress, backlog: Int = 0): Unit = withContext(Dispatchers.IO) {
        channel.bind(local, backlog)
    }

    override suspend fun close(): Unit = acceptMutex.withLock {
        channel.close()
        acceptTimeout = null
    }

    fun getChannel(): AsynchronousServerSocketChannel = channel

    fun getInetAddress(): InetAddress?  {
        val address = channel.localAddress ?: return null
        return if(address is InetSocketAddress) {
            address.address
        } else {
            null
        }
    }

    fun getLocalPort(): Int {
        val address = channel.localAddress ?: return -1
        return if(address is InetSocketAddress) {
            address.port
        } else {
            -1
        }
    }

    fun getLocalSocketAddress(): SocketAddress? = channel.localAddress
    fun getReceiveBufferSize(): Int? = channel.getOption(java.net.StandardSocketOptions.SO_RCVBUF)
    fun getReuseAddress(): Boolean? = channel.getOption(java.net.StandardSocketOptions.SO_REUSEADDR)
    fun getAcceptTimeout(): Long? = acceptTimeout
    fun isBound(): Boolean = channel.localAddress != null
    fun isClosed(): Boolean = !channel.isOpen
    fun setReceiveBufferSize(size: Int) {
        channel.setOption(java.net.StandardSocketOptions.SO_RCVBUF, size)
    }
    fun setReuseAddress(reuse: Boolean) {
        channel.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, reuse)
    }
    fun setAcceptTimeout(timeout: Long) {
        require(timeout >= 0) { "timeout value is negative" }
        this.acceptTimeout = timeout
    }
}