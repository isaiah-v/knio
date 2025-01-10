package org.ivcode.knio.net

import org.ivcode.knio.lang.KAutoCloseable
import org.ivcode.org.ivcode.knio.net.KSocketOutputStream
import org.jetbrains.annotations.Blocking
import java.io.IOException
import java.net.SocketAddress
import java.nio.channels.AsynchronousSocketChannel

/**
 * A coroutine-based socket implementation using AsynchronousSocketChannel.
 *
 * [Blocking] - Though the underlying nio implementation is non-blocking, some operations may block briefly. In most
 * cases this will be negligible. It's important to note the underlying does not set the dispatcher for operations.
 * It is up to the user to decide the appropriate dispatcher.
 *
 */
@Blocking
interface KSocket: KAutoCloseable {


    /**
     * Binds the socket to a local address.
     * If the address is null, then the system will pick up an ephemeral port and a valid local address to bind the socket.
     *
     * @param local the SocketAddress to bind to
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    suspend fun bind(local: SocketAddress? = null)

    /**
     * Closes the socket.
     */
    @Throws(IOException::class)
    override suspend fun close()

    /**
     * Connects this channel.
     *
     * @param endpoint The address to connect to
     * @param timeout The timeout in milliseconds, or 0 for no timeout
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    suspend fun connect(endpoint: SocketAddress, timeout: Long = 0)

    /**
     * Gets the underlying AsynchronousSocketChannel.
     *
     * @return The AsynchronousSocketChannel.
     */
    suspend fun getChannel(): AsynchronousSocketChannel

    /**
     * Gets the remote InetAddress.
     *
     * @return The remote InetAddress, or null if not connected.
     */
    suspend fun getInetAddress(): java.net.InetAddress?

    /**
     * Gets the input stream of the socket.
     *
     * @return The KInputStream.
     */
    fun getInputStream(): KSocketInputStream

    /**
     * Gets the SO_KEEPALIVE option.
     *
     * @return The value of the SO_KEEPALIVE option.
     */
    @Throws(IOException::class)
    suspend fun getKeepAlive(): Boolean

    /**
     * Gets the local InetAddress.
     *
     * @return The local InetAddress, or null if not bound.
     */
    suspend fun getLocalAddress(): java.net.InetAddress?

    /**
     * Gets the local port number.
     *
     * @return The local port number, or -1 if not bound.
     */
    suspend fun getLocalPort(): Int

    /**
     * Gets the local socket address.
     *
     * @return The local SocketAddress.
     */
    suspend fun getLocalSocketAddress(): SocketAddress?

    /**
     * Gets the output stream of the socket.
     *
     * @return The KOutputStream.a
     */
    fun getOutputStream(): KSocketOutputStream

    /**
     * Gets the remote port number.
     *
     * @return The remote port number, or -1 if not connected.
     */
    suspend fun getPort(): Int

    /**
     * Gets the SO_RCVBUF option.
     *
     * @return The value of the SO_RCVBUF option.
     */
    @Throws(IOException::class)
    suspend fun getReceiveBufferSize(): Int

    /**
     * Gets the remote socket address.
     *
     * @return The remote SocketAddress.
     */
    suspend fun getRemoteSocketAddress(): SocketAddress

    /**
     * Gets the SO_REUSEADDR option.
     *
     * @return The value of the SO_REUSEADDR option.
     */
    @Throws(IOException::class)
    suspend fun getReuseAddress(): Boolean

    /**
     * Gets the SO_SNDBUF option.
     *
     * @return The value of the SO_SNDBUF option.
     */
    @Throws(IOException::class)
    suspend fun getSendBufferSize(): Int

    /**
     * Gets the SO_LINGER option.
     *
     * @return The value of the SO_LINGER option.
     */
    @Throws(IOException::class)
    suspend fun getSoLinger(): Int

    /**
     * Gets the read timeout.
     *
     * @return The read timeout in milliseconds.
     */
    suspend fun getReadTimeout(): Long

    /**
     * Gets the write timeout.
     *
     * @return The write timeout in milliseconds.
     */
    suspend fun getWriteTimeout(): Long

    /**
     * Gets the TCP_NODELAY option.
     *
     * @return The value of the TCP_NODELAY option.
     */
    @Throws(IOException::class)
    suspend fun getTcpNoDelay(): Boolean

    /**
     * Gets the IP_TOS option.
     *
     * @return The value of the IP_TOS option.
     */
    @Throws(IOException::class)
    suspend fun getTrafficClass(): Int

    /**
     * Checks if the socket is bound.
     *
     * @return True if the socket is bound, false otherwise.
     */
    suspend fun isBound(): Boolean

    /**
     * Checks if the socket is closed.
     *
     * @return True if the socket is closed, false otherwise.
     */
    suspend fun isClosed(): Boolean

    /**
     * Checks if the socket is connected.
     *
     * @return True if the socket is connected, false otherwise.
     */
    suspend fun isConnected(): Boolean

    /**
     * Checks if the input is shutdown.
     *
     * @return True if the input is shutdown, false otherwise.
     */
    suspend fun isInputShutdown(): Boolean

    /**
     * Checks if the output is shutdown.
     *
     * @return True if the output is shutdown, false otherwise.
     */
    suspend fun isOutputShutdown(): Boolean

    /**
     * Sets the SO_KEEPALIVE option.
     *
     * @param keepAlive The value to set.
     */
    @Throws(IOException::class)
    suspend fun setKeepAlive(keepAlive: Boolean)

    /**
     * Sets the SO_RCVBUF option.
     *
     * @param size The buffer size to set.
     */
    @Throws(IOException::class)
    suspend fun setReceiveBufferSize(size: Int)

    /**
     * Sets the SO_REUSEADDR option.
     *
     * @param reuse The value to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun setReuseAddress(reuse: Boolean)

    /**
     * Sets the SO_SNDBUF option.
     *
     * @param size The buffer size to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun setSendBufferSize(size: Int)

    /**
     * Sets the read timeout.
     *
     * @param timeout The timeout in milliseconds, or null to disable.
     */
    suspend fun setReadTimeout(timeout: Long?)

    /**
     * Sets the write timeout.
     *
     * @param timeout The timeout in milliseconds, or null to disable.
     */
    suspend fun setWriteTimeout(timeout: Long?)

    /**
     * Sets the TCP_NODELAY option.
     *
     * @param on The value to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun setTcpNoDelay(on: Boolean)

    /**
     * Sets the IP_TOS option.
     *
     * @param tc The value to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun setTrafficClass(tc: Int)

    /**
     * Shuts down the input side of the socket.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun shutdownInput()


    /**
     * Shuts down the output side of the socket.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun shutdownOutput()
}