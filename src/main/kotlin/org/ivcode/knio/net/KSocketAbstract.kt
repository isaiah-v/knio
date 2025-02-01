package org.ivcode.knio.net

import org.ivcode.knio.utils.asCompletionHandler
import org.ivcode.knio.utils.timeout
import java.io.IOException
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

    /**
     * Binds the socket to a local address.
     * If the address is null, then the system will pick up an ephemeral port and a valid local address to bind the socket.
     *
     * @param local the SocketAddress to bind to
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override suspend fun bind(local: SocketAddress?) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.bind(local)
    }

    /**
     * Closes the socket.
     */
    @Throws(IOException::class)
    override suspend fun close() {
        if (!ch.isOpen) return

        @Suppress("BlockingMethodInNonBlockingContext")
        ch.close()
    }

    /**
     * Connects this channel.
     *
     * @param endpoint The address to connect to
     * @param timeout The timeout in milliseconds, or 0 for no timeout
     * @throws IOException if an I/O error occurs
     */
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

    /**
     * Gets the underlying AsynchronousSocketChannel.
     *
     * @return The AsynchronousSocketChannel.
     */
    override suspend fun getChannel(): AsynchronousSocketChannel = ch

    /**
     * Gets the remote InetAddress.
     *
     * @return The remote InetAddress, or null if not connected.
     */
    override suspend fun getInetAddress(): java.net.InetAddress? {
        val address = ch.remoteAddress ?: return null
        return if(address is java.net.InetSocketAddress) {
            address.address
        } else {
            null
        }
    }

    /**
     * Gets the SO_KEEPALIVE option.
     *
     * @return The value of the SO_KEEPALIVE option.
     */
    @Throws(IOException::class)
    override suspend fun getKeepAlive(): Boolean {
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.SO_KEEPALIVE)
    }

    /**
     * Gets the local InetAddress.
     *
     * @return The local InetAddress, or null if not bound.
     */
    override suspend fun getLocalAddress(): java.net.InetAddress? {
        val address = ch.localAddress ?: null
        return if(address is java.net.InetSocketAddress) {
            address.address
        } else {
            null
        }
    }

    /**
     * Gets the local port number.
     *
     * @return The local port number, or -1 if not bound.
     */
    override suspend fun getLocalPort(): Int {
        val address = ch.localAddress ?: return -1
        return if(address is java.net.InetSocketAddress) {
            address.port
        } else {
            -1
        }
    }

    /**
     * Gets the local socket address.
     *
     * @return The local SocketAddress.
     */
    override suspend fun getLocalSocketAddress(): SocketAddress? = ch.localAddress



    /**
     * Gets the remote port number.
     *
     * @return The remote port number, or -1 if not connected.
     */
    override suspend fun getPort(): Int {
        val address = ch.remoteAddress ?: return -1
        return if(address is java.net.InetSocketAddress) {
            address.port
        } else {
            -1
        }
    }

    /**
     * Gets the SO_RCVBUF option.
     *
     * @return The value of the SO_RCVBUF option.
     */
    @Throws(IOException::class)
    override suspend fun getReceiveBufferSize(): Int {
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.SO_RCVBUF)
    }

    /**
     * Gets the remote socket address.
     *
     * @return The remote SocketAddress.
     */
    override suspend fun getRemoteSocketAddress(): SocketAddress = ch.remoteAddress

    /**
     * Gets the SO_REUSEADDR option.
     *
     * @return The value of the SO_REUSEADDR option.
     */
    @Throws(IOException::class)
    override suspend fun getReuseAddress(): Boolean {
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.SO_REUSEADDR)
    }

    /**
     * Gets the SO_SNDBUF option.
     *
     * @return The value of the SO_SNDBUF option.
     */
    @Throws(IOException::class)
    override suspend fun getSendBufferSize(): Int{
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.SO_SNDBUF)
    }

    /**
     * Gets the SO_LINGER option.
     *
     * @return The value of the SO_LINGER option.
     */
    @Throws(IOException::class)
    override suspend fun getSoLinger(): Int {
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.SO_LINGER)
    }

    /**
     * Gets the read timeout.
     *
     * @return The read timeout in milliseconds.
     */
    override suspend fun getReadTimeout(): Long = this.rTimeout ?: 0

    /**
     * Gets the write timeout.
     *
     * @return The write timeout in milliseconds.
     */
    override suspend fun getWriteTimeout(): Long = this.wTimeout ?: 0

    /**
     * Gets the TCP_NODELAY option.
     *
     * @return The value of the TCP_NODELAY option.
     */
    @Throws(IOException::class)
    override suspend fun getTcpNoDelay(): Boolean {
        @Suppress("BlockingMethodInNonBlockingContext")
        return ch.getOption(java.net.StandardSocketOptions.TCP_NODELAY)
    }

    /**
     * Checks if the socket is bound.
     *
     * @return True if the socket is bound, false otherwise.
     */
    override suspend fun isBound(): Boolean = ch.localAddress != null

    /**
     * Checks if the socket is closed.
     *
     * @return True if the socket is closed, false otherwise.
     */
    override suspend fun isClosed(): Boolean = !ch.isOpen

    /**
     * Checks if the socket is connected.
     *
     * @return True if the socket is connected, false otherwise.
     */
    override suspend fun isConnected(): Boolean = ch.remoteAddress != null

    /**
     * Sets the SO_KEEPALIVE option.
     *
     * @param keepAlive The value to set.
     */
    @Throws(IOException::class)
    override suspend fun setKeepAlive(keepAlive: Boolean) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.setOption(java.net.StandardSocketOptions.SO_KEEPALIVE, keepAlive)
    }

    /**
     * Sets the SO_RCVBUF option.
     *
     * @param size The buffer size to set.
     */
    @Throws(IOException::class)
    override suspend fun setReceiveBufferSize(size: Int) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.setOption(java.net.StandardSocketOptions.SO_RCVBUF, size)
    }

    /**
     * Sets the SO_REUSEADDR option.
     *
     * @param reuse The value to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun setReuseAddress(reuse: Boolean) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, reuse)
    }

    /**
     * Sets the SO_SNDBUF option.
     *
     * @param size The buffer size to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun setSendBufferSize(size: Int) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.setOption(java.net.StandardSocketOptions.SO_SNDBUF, size)
    }

    /**
     * Sets the read timeout.
     *
     * @param timeout The timeout in milliseconds, or null to disable.
     */
    override suspend fun setReadTimeout(timeout: Long?) {
        if(timeout==null || timeout==0L) {
            this.rTimeout = null
        } else if(timeout<0) {
            throw IllegalArgumentException("Timeout must be greater than or equal to 0")
        } else {
            this.rTimeout = timeout
        }
    }

    /**
     * Sets the write timeout.
     *
     * @param timeout The timeout in milliseconds, or null to disable.
     */
    override suspend fun setWriteTimeout(timeout: Long?) {
        if(timeout==null || timeout==0L) {
            this.wTimeout = null
        } else if(timeout<0) {
            throw IllegalArgumentException("Timeout must be greater than or equal to 0")
        } else {
            this.wTimeout = timeout
        }
    }

    /**
     * Sets the TCP_NODELAY option.
     *
     * @param on The value to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun setTcpNoDelay(on: Boolean) {
        @Suppress("BlockingMethodInNonBlockingContext")
        ch.setOption(java.net.StandardSocketOptions.TCP_NODELAY, on)
    }
}