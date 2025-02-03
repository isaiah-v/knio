package org.ivcode.knio.net

import org.ivcode.knio.utils.asCompletionHandler
import org.ivcode.knio.utils.timeout
import java.net.SocketAddress
import java.net.SocketTimeoutException
import java.nio.channels.AsynchronousSocketChannel
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal abstract class KSocketAbstract(
    protected val ch: AsynchronousSocketChannel
): KSocket {



    /** The read timeout in milliseconds. */
    private var rTimeout: Long? = null

    /** The write timeout in milliseconds. */
    private var wTimeout: Long? = null


    override suspend fun bind(local: SocketAddress?) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.bind(local)
    }


    override suspend fun close() {
        if (!ch.isOpen) return

        @Suppress("BlockingMethodInNonBlockingContext")
        ch.close()
    }


    override suspend fun connect(endpoint: SocketAddress, timeout: Long) = suspendCoroutine {
        try {
            val timoutJob = if (timeout > 0) {
                it.timeout(timeout) { SocketTimeoutException("Connection timed out") }
            } else if (timeout<0) {
                throw IllegalArgumentException("Timeout must be greater than or equal to 0")
            } else {
                null
            }

            // returns "this" upon completion
            ch.connect(endpoint, it, Unit.asCompletionHandler(timoutJob))
        } catch (e: Throwable) {
            it.resumeWithException(e)
        }
    }


    override suspend fun getInetAddress(): java.net.InetAddress? {
        val address = ch.remoteAddress ?: return null
        return if(address is java.net.InetSocketAddress) {
            address.address
        } else {
            null
        }
    }


    override suspend fun getKeepAlive(): Boolean {
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.SO_KEEPALIVE)
    }


    override suspend fun getLocalAddress(): java.net.InetAddress? {
        val address = ch.localAddress ?: null
        return if(address is java.net.InetSocketAddress) {
            address.address
        } else {
            null
        }
    }


    override suspend fun getLocalPort(): Int {
        val address = ch.localAddress ?: return -1
        return if(address is java.net.InetSocketAddress) {
            address.port
        } else {
            -1
        }
    }


    override suspend fun getLocalSocketAddress(): SocketAddress? =
        ch.localAddress


    override suspend fun getPort(): Int {
        val address = ch.remoteAddress ?: return -1
        return if(address is java.net.InetSocketAddress) {
            address.port
        } else {
            -1
        }
    }


    override suspend fun getReceiveBufferSize(): Int {
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.SO_RCVBUF)
    }


    override suspend fun getRemoteSocketAddress(): SocketAddress = ch.remoteAddress


    override suspend fun getReuseAddress(): Boolean {
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.SO_REUSEADDR)
    }

    override suspend fun getSendBufferSize(): Int{
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.SO_SNDBUF)
    }

    override suspend fun getReadTimeout(): Long =
        this.rTimeout ?: 0


    override suspend fun getWriteTimeout(): Long =
        this.wTimeout ?: 0


    override suspend fun getTcpNoDelay(): Boolean {
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.TCP_NODELAY)
    }

    override suspend fun isBound(): Boolean =
        ch.localAddress != null


    override suspend fun isClosed(): Boolean =
        !ch.isOpen


    override suspend fun isConnected(): Boolean =
        ch.remoteAddress != null


    override suspend fun setKeepAlive(keepAlive: Boolean) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.setOption(java.net.StandardSocketOptions.SO_KEEPALIVE, keepAlive)
    }


    override suspend fun setReceiveBufferSize(size: Int) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.setOption(java.net.StandardSocketOptions.SO_RCVBUF, size)
    }


    override suspend fun setReuseAddress(reuse: Boolean) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, reuse)
    }


    override suspend fun setSendBufferSize(size: Int) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.setOption(java.net.StandardSocketOptions.SO_SNDBUF, size)
    }


    override suspend fun setReadTimeout(timeout: Long?) {
        if(timeout==null || timeout==0L) {
            this.rTimeout = null
        } else if(timeout<0) {
            throw IllegalArgumentException("Timeout must be greater than or equal to 0")
        } else {
            this.rTimeout = timeout
        }
    }


    override suspend fun setWriteTimeout(timeout: Long?) {
        if(timeout==null || timeout==0L) {
            this.wTimeout = null
        } else if(timeout<0) {
            throw IllegalArgumentException("Timeout must be greater than or equal to 0")
        } else {
            this.wTimeout = timeout
        }
    }


    override suspend fun setTcpNoDelay(on: Boolean) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.setOption(java.net.StandardSocketOptions.TCP_NODELAY, on)
    }
}