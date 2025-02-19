package org.knio.core.net

import org.knio.core.io.KInputStream
import org.knio.core.io.KOutputStream
import org.knio.core.lang.KAutoCloseable
import java.nio.channels.AsynchronousSocketChannel
import java.io.IOException
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException

import java.net.SocketOptions.*

/**
 * This class implements client sockets (also called just "sockets"). A socket is an endpoint for communication between
 * two machines.
 *
 * @see Socket
 * @see AsynchronousSocketChannel
 */
interface KSocket: KAutoCloseable {


    /**
     * Binds the socket to a local address.
     * If the address is null, then the system will pick up an ephemeral port and a valid local address to bind the socket.
     *
     * @param local the SocketAddress to bind to
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the address is a SocketAddress subclass not supported by this socket
     * @throws IllegalStateException if the socket is already bound
     *
     * @see Socket.bind
     * @see AsynchronousSocketChannel.bind
     */
    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    suspend fun bind(local: SocketAddress?)

    /**
     * Closes this socket.
     *
     * Any job currently suspended in an I/O operation upon this socket will throw a SocketException.
     *
     * Once a socket has been closed, it is not available for further networking use (i.e. can't be reconnected or
     * rebound). A new socket needs to be created.
     *
     * Closing this socket will also close the socket's KInputStream and KOutputStream.
     *
     * @throws IOException if an I/O error occurs
     *
     * @see Socket.close
     * @see AsynchronousSocketChannel.close
     */
    @Throws(IOException::class)
    override suspend fun close()

    /**
     * Connects this socket to the server with a specified timeout value. A timeout of zero is interpreted as an
     * infinite timeout. The connection will then suspend until established or an error occurs.
     *
     * @param endpoint The address to connect to
     *
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the endpoint is a SocketAddress subclass not supported by this socket
     *
     * @see java.net.Socket.connect
     * @see AsynchronousSocketChannel.connect
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    suspend fun connect(endpoint: SocketAddress)

    /**
     * Returns the address to which the socket is connected.
     *
     * If the socket was connected prior to being closed, then this method will continue to return the connected address
     * after the socket is closed.
     *
     * @return The remote InetAddress, or null if not connected.
     *
     * @see Socket.getInetAddress
     * @see AsynchronousSocketChannel.getRemoteAddress
     */
    suspend fun getInetAddress(): java.net.InetAddress?

    /**
     * Returns the [KInputStream] for this socket.
     *
     * Closing the returned [KInputStream] will close the associated socket.
     *
     * @return The KInputStream.
     *
     * @throws IOException if an I/O error occurs when creating the input stream, the socket is closed, the socket is
     * not connected, or the socket input has been shutdown using shutdownInput()
     *
     * @see Socket.getInputStream
     */
    @Throws(IOException::class)
    suspend fun getInputStream(): KInputStream

    /**
     * Tests if [SO_KEEPALIVE] is enabled.
     *
     * @return a boolean indicating whether [SO_KEEPALIVE] is enabled.
     *
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     *
     * @see Socket.getKeepAlive
     * @see AsynchronousSocketChannel.getOption
     * @see SO_KEEPALIVE
     */
    @Throws(SocketException::class)
    suspend fun getKeepAlive(): Boolean

    /**
     * Gets the local address to which the socket is bound.
     *
     * If there is a security manager set, its checkConnect method is called with the local address and -1 as its
     * arguments to see if the operation is allowed. If the operation is not allowed, the loopback address is returned.
     *
     * @return the local address to which the socket is bound, the loopback address if denied by the security manager,
     * or the wildcard address if the socket is closed or not bound yet
     *
     * @see Socket.getLocalAddress
     * @see AsynchronousSocketChannel.getLocalAddress
     */
    suspend fun getLocalAddress(): java.net.InetAddress

    /**
     * Returns the local port number to which this socket is bound.
     *
     * If the socket was bound prior to being closed, then this method will continue to return the local port number
     * after the socket is closed.
     *
     * @return The local port number, or -1 if not bound.
     *
     * @see Socket.getLocalPort
     * @see AsynchronousSocketChannel.getLocalAddress
     */
    suspend fun getLocalPort(): Int

    /**
     * Returns the address of the endpoint this socket is bound to.
     *
     * If a socket bound to an endpoint represented by an InetSocketAddress is closed, then this method will continue to
     * return an InetSocketAddress after the socket is closed. In that case the returned InetSocketAddress's address is
     * the wildcard address and its port is the local port that it was bound to.
     *
     * If there is a security manager set, its checkConnect method is called with the local address and -1 as its
     * arguments to see if the operation is allowed. If the operation is not allowed, a SocketAddress representing the
     * loopback address and the local port to which this socket is bound is returned.
     *
     * @return The local SocketAddress.
     *
     * @see Socket.getLocalSocketAddress
     * @see AsynchronousSocketChannel.getLocalAddress
     */
    suspend fun getLocalSocketAddress(): SocketAddress?

    /**
     * Returns a [KOutputStream] for this socket.
     *
     * Closing the returned [KOutputStream] will close the associated socket.
     *
     * @return an output stream for writing bytes to this socket.
     *
     * @throws IOException if an I/O error occurs when creating the output stream or if the socket is not connected.
     *
     * @see Socket.getOutputStream
     */
    @Throws(IOException::class)
    suspend fun getOutputStream(): KOutputStream

    /**
     * Returns the remote port number to which this socket is connected.
     *
     * If the socket was connected prior to being closed, then this method will continue to return the connected port
     * number after the socket is closed.
     *
     * @return The remote port number, or -1 if not connected.
     *
     * @see Socket.getLocalPort
     * @see AsynchronousSocketChannel.getRemoteAddress
     */
    suspend fun getPort(): Int

    /**
     * Gets the value of the [SO_RCVBUF] option for this Socket, that is the buffer size used by the platform for input on
     * this Socket.
     *
     * @return the value of the [SO_RCVBUF] option for this Socket.
     *
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     *
     * @see Socket.getReceiveBufferSize
     * @see AsynchronousSocketChannel.getOption
     * @see SO_RCVBUF
     */
    @Throws(SocketException::class)
    suspend fun getReceiveBufferSize(): Int

    /**
     * Returns the address of the endpoint this socket is connected to, or null if it is unconnected.
     *
     * If the socket was connected prior to being closed, then this method will continue to return the connected address
     * after the socket is closed.
     *
     * @return a [SocketAddress] representing the remote endpoint of this socket, or `null` if it is not connected yet.
     */
    suspend fun getRemoteSocketAddress(): SocketAddress?

    /**
     * Tests if [SO_REUSEADDR] is enabled.
     *
     * @return a boolean indicating whether [SO_REUSEADDR] is enabled.
     *
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     */
    @Throws(SocketException::class)
    suspend fun getReuseAddress(): Boolean

    /**
     * Get value of the [SO_SNDBUF] option for this Socket, that is the buffer size used by the platform for output on
     * this Socket.
     *
     * @return the value of the [SO_SNDBUF] option for this Socket.
     *
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     */
    @Throws(SocketException::class)
    suspend fun getSendBufferSize(): Int

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
     * Tests if [TCP_NODELAY] is enabled.
     *
     * @return a boolean indicating whether [TCP_NODELAY] is enabled.
     *
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     */
    @Throws(SocketException::class)
    suspend fun getTcpNoDelay(): Boolean

    /**
     * Returns the binding state of the socket.
     *
     * Note: Closing a socket doesn't clear its binding state, which means this method will return true for a closed
     * socket (see isClosed()) if it was successfully bound prior to being closed.
     *
     * @return `true` if the socket was successfully bound to an address
     */
    suspend fun isBound(): Boolean

    /**
     * Returns the closed state of the socket.
     *
     * @return `true` if the socket has been closed
     */
    suspend fun isClosed(): Boolean

    /**
     * Returns the connection state of the socket.
     *
     * Note: Closing a socket doesn't clear its connection state, which means this method will return true for a closed
     * socket (see isClosed()) if it was successfully connected prior to being closed.
     *
     * @return `true` if the socket is connected, `false` otherwise.
     */
    suspend fun isConnected(): Boolean

    /**
     * Returns whether the read-half of the socket connection is closed.
     *
     * @return `true` if the input of the socket has been shutdown
     */
    suspend fun isInputShutdown(): Boolean

    /**
     * Returns whether the write-half of the socket connection is closed.
     *
     * @return `true` if the output of the socket has been shutdown
     */
    suspend fun isOutputShutdown(): Boolean

    /**
     * Enable/disable [SO_KEEPALIVE].
     *
     * @param keepAlive whether to have socket keep alive turned on.
     *
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     */
    @Throws(SocketException::class)
    suspend fun setKeepAlive(keepAlive: Boolean)

    /**
     * Sets the [SO_RCVBUF] option to the specified value for this Socket. The [SO_RCVBUF] option is used by the
     * platform's networking code as a hint for the size to set the underlying network I/O buffers.
     *
     * Increasing the receive buffer size can increase the performance of network I/O for high-volume connection,
     * while decreasing it can help reduce the backlog of incoming data.
     *
     * Because [SO_RCVBUF] is a hint, applications that want to verify what size the buffers were set to should call
     * getReceiveBufferSize().
     *
     * The value of [SO_RCVBUF] is also used to set the TCP receive window that is advertised to the remote peer.
     * Generally, the window size can be modified at any time when a socket is connected. However, if a receive window
     * larger than 64K is required then this must be requested before the socket is connected to the remote peer. There
     * are two cases to be aware of:
     *
     *  1. For sockets accepted from a ServerSocket, this must be done by calling ServerSocket.setReceiveBufferSize(int)
     *  before the ServerSocket is bound to a local address.
     *
     *  2. For client sockets, setReceiveBufferSize() must be called before connecting the socket to its remote peer.
     *
     * @param size The buffer size to set.
     *
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     * @throws IllegalArgumentException if the value is 0 or negative.
     */
    @Throws(SocketException::class, IllegalArgumentException::class)
    suspend fun setReceiveBufferSize(size: Int)

    /**
     * Enable/disable the [SO_REUSEADDR] socket option.
     *
     * When a TCP connection is closed the connection may remain in a timeout state for a period of time after the
     * connection is closed (typically known as the TIME_WAIT state or 2MSL wait state). For applications using a well
     * known socket address or port it may not be possible to bind a socket to the required SocketAddress if there is a
     * connection in the timeout state involving the socket address or port.
     *
     * Enabling [SO_REUSEADDR] prior to binding the socket using bind(SocketAddress) allows the socket to be bound even
     * though a previous connection is in a timeout state.
     *
     * When a Socket is created the initial setting of [SO_REUSEADDR] is disabled.
     *
     * The behaviour when [SO_REUSEADDR] is enabled or disabled after a socket is bound (See isBound()) is not defined.
     *
     * @param reuse whether to enable or disable the [SO_REUSEADDR] socket option.
     *
     * @throws SocketException if an error occurs enabling or disabling the [SO_REUSEADDR] socket option, or the socket
     * is closed.
     */
    @Throws(SocketException::class)
    suspend fun setReuseAddress(reuse: Boolean)

    /**
     * Sets the [SO_SNDBUF] option to the specified value for this Socket. The [SO_SNDBUF] option is used by the
     * platform's networking code as a hint for the size to set the underlying network I/O buffers.
     *
     * Because [SO_SNDBUF] is a hint, applications that want to verify what size the buffers were set to should cal
     * getSendBufferSize().
     *
     * @param size the size to which to set the send buffer size. This value must be greater than 0.
     *
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     * @throws IllegalArgumentException if the value is 0 or negative.
     */
    @Throws(SocketException::class, IllegalArgumentException::class)
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
     * Enable/disable [TCP_NODELAY] (disable/enable Nagle's algorithm).
     *
     * @param on `true` to enable [TCP_NODELAY], `false` to disable.
     *
     * @throws SocketException if there is an error in the underlying protocol, such as a TCP error.
     */
    @Throws(SocketException::class)
    suspend fun setTcpNoDelay(on: Boolean)

    /**
     * Places the input stream for this socket at "end of stream". Any data sent to the input stream side of the socket
     * is acknowledged and then silently discarded.
     *
     * If you read from a socket input stream after invoking this method on the socket, the stream's available method
     * will return 0, and its read methods will return -1 (end of stream).
     *
     * @throws IOException if an I/O error occurs when shutting down this socket.
     */
    @Throws(IOException::class)
    suspend fun shutdownInput()


    /**
     * Disables the output stream for this socket. For a TCP socket, any previously written data will be sent followed
     * by TCP's normal connection termination sequence. If you write to a socket output stream after invoking
     * `shutdownOutput()` on the socket, the stream will throw an `IOException`.
     *
     * @throws IOException if an I/O error occurs when shutting down this socket.
     */
    @Throws(IOException::class)
    suspend fun shutdownOutput()
}