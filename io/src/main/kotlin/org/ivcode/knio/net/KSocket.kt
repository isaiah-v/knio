package org.ivcode.knio.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ivcode.knio.lang.KAutoCloseable
import org.ivcode.knio.io.KInputStream
import org.ivcode.knio.io.KOutputStream
import org.ivcode.knio.utils.asCompletionHandler
import org.ivcode.knio.utils.fromResult
import org.ivcode.knio.utils.timeout
import java.io.IOException
import java.net.SocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.InterruptedByTimeoutException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * A coroutine-based socket implementation using AsynchronousSocketChannel.
 */
interface KSocket: KAutoCloseable {


    /**
     * Binds the socket to a local address.
     * If the address is null, then the system will pick up an ephemeral port and a valid local address to bind the socket.
     *
     * @param local the SocketAddress to bind to
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun bind(local: SocketAddress? = null)

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
    suspend fun connect(endpoint: SocketAddress, timeout: Long = 0)

    /**
     * Gets the underlying AsynchronousSocketChannel.
     *
     * @return The AsynchronousSocketChannel.
     */
    fun getChannel(): AsynchronousSocketChannel

    /**
     * Gets the remote InetAddress.
     *
     * @return The remote InetAddress, or null if not connected.
     */
    fun getInetAddress(): java.net.InetAddress?

    /**
     * Gets the input stream of the socket.
     *
     * @return The KInputStream.
     */
    fun getInputStream(): KInputStream

    /**
     * Gets the SO_KEEPALIVE option.
     *
     * @return The value of the SO_KEEPALIVE option.
     */
    fun getKeepAlive(): Boolean

    /**
     * Gets the local InetAddress.
     *
     * @return The local InetAddress, or null if not bound.
     */
    fun getLocalAddress(): java.net.InetAddress?

    /**
     * Gets the local port number.
     *
     * @return The local port number, or -1 if not bound.
     */
    fun getLocalPort(): Int

    /**
     * Gets the local socket address.
     *
     * @return The local SocketAddress.
     */
    fun getLocalSocketAddress(): SocketAddress?

    /**
     * Gets the output stream of the socket.
     *
     * @return The KOutputStream.
     */
    fun getOutputStream(): KOutputStream

    /**
     * Gets the remote port number.
     *
     * @return The remote port number, or -1 if not connected.
     */
    fun getPort(): Int

    /**
     * Gets the SO_RCVBUF option.
     *
     * @return The value of the SO_RCVBUF option.
     */
    fun getReceiveBufferSize(): Int

    /**
     * Gets the remote socket address.
     *
     * @return The remote SocketAddress.
     */
    fun getRemoteSocketAddress(): SocketAddress

    /**
     * Gets the SO_REUSEADDR option.
     *
     * @return The value of the SO_REUSEADDR option.
     */
    fun getReuseAddress(): Boolean

    /**
     * Gets the SO_SNDBUF option.
     *
     * @return The value of the SO_SNDBUF option.
     */
    fun getSendBufferSize(): Int

    /**
     * Gets the SO_LINGER option.
     *
     * @return The value of the SO_LINGER option.
     */
    fun getSoLinger(): Int

    /**
     * Gets the read timeout.
     *
     * @return The read timeout in milliseconds.
     */
    fun getReadTimeout(): Long

    /**
     * Gets the write timeout.
     *
     * @return The write timeout in milliseconds.
     */
    fun getWriteTimeout(): Long

    /**
     * Gets the TCP_NODELAY option.
     *
     * @return The value of the TCP_NODELAY option.
     */
    fun getTcpNoDelay(): Boolean

    /**
     * Gets the IP_TOS option.
     *
     * @return The value of the IP_TOS option.
     */
    fun getTrafficClass(): Int

    /**
     * Checks if the socket is bound.
     *
     * @return True if the socket is bound, false otherwise.
     */
    fun isBound(): Boolean

    /**
     * Checks if the socket is closed.
     *
     * @return True if the socket is closed, false otherwise.
     */
    fun isClosed(): Boolean

    /**
     * Checks if the socket is connected.
     *
     * @return True if the socket is connected, false otherwise.
     */
    fun isConnected(): Boolean

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
    fun setReadTimeout(timeout: Long?)

    /**
     * Sets the write timeout.
     *
     * @param timeout The timeout in milliseconds, or null to disable.
     */
    fun setWriteTimeout(timeout: Long?)

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