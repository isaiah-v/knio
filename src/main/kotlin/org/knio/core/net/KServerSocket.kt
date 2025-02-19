package org.knio.core.net

import org.knio.core.lang.KAutoCloseable
import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.SocketOptions.*

/**
 * This class implements server sockets. A server socket waits for requests to come in over the network. It performs
 * some operation based on that request, and then possibly returns a result to the requester.
 *
 * @see java.net.ServerSocket
 * @see java.nio.channels.AsynchronousServerSocketChannel
 */
interface KServerSocket: KAutoCloseable {

    /**
     * Listens for a connection to be made to this socket and accepts it. The method blocks until a connection is made.
     *
     * A new Socket s is created and, if there is a security manager, the security manager's checkAccept method is
     * called with s.getInetAddress().getHostAddress() and s.getPort() as its arguments to ensure the operation is
     * allowed. This could result in a SecurityException.
     *
     * @return the new Socket
     *
     * @throws IOException if an I/O error occurs when waiting for a connection.
     * @throws SecurityException if a security manager exists and its checkAccept method doesn't allow the operation.
     * @throws SocketTimeoutException if a timeout was previously set with setSoTimeout and the timeout has beenreached.
     *
     * @see java.net.ServerSocket.accept
     * @see java.nio.channels.AsynchronousServerSocketChannel.accept
     */
    @Throws(IOException::class, SecurityException::class, SocketTimeoutException::class)
    suspend fun accept(): KSocket

    /**
     * Binds the ServerSocket to a specific address (IP address and port number).
     *
     * If the address is null, then the system will pick up an ephemeral port and a valid local address to bind the
     * socket.
     *
     * The backlog argument is the requested maximum number of pending connections on the socket. Its exact semantics
     * are implementation specific. In particular, an implementation may impose a maximum length or may choose to ignore
     * the parameter altogther. The value provided should be greater than 0. If it is less than or equal to 0, then an
     * implementation specific default will be used.
     *
     * @param endpoint The IP address and port number to bind to.
     * @param backlog The maximum number of pending connections.
     *
     * @throws IOException if an I/O error occurs when binding the socket.
     * @throws IllegalArgumentException if endpoint is a SocketAddress subclass not supported by this socket.
     * @throws SecurityException if a security manager exists and its checkListen method doesn't allow the operation.
     *
     * @see java.net.ServerSocket.bind
     * @see java.nio.channels.AsynchronousServerSocketChannel.bind
     */
    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class)
    suspend fun bind(endpoint: SocketAddress, backlog: Int = 0)

    /**
     * Closes this socket. Any thread currently blocked in accept() will throw a SocketException.
     *
     * If this socket has an associated channel then the channel is closed as well.
     *
     * @throws IOException if an I/O error occurs when closing the socket.
     *
     * @see java.net.ServerSocket.close
     * @see java.nio.channels.AsynchronousServerSocketChannel.close
     */
    @Throws(IOException::class)
    override suspend fun close()

    /**
     * Returns the local address of this server socket.
     *
     * If the socket was bound prior to being closed, then this method will continue to return the local address after
     * the socket is closed.
     *
     * If there is a security manager set, its checkConnect method is called with the local address and -1 as its
     * arguments to see if the operation is allowed. If the operation is not allowed, the loopback address is returned.
     *
     * @return the address to which this socket is bound, or the loopback address if denied by the security manager, or
     * `null` if the socket is unbound.
     *
     * @see java.net.ServerSocket.getInetAddress
     * @see java.nio.channels.AsynchronousServerSocketChannel.getLocalAddress
     */
    suspend fun getInetAddress(): InetAddress?

    /**
     * Returns the port number on which this socket is listening.
     *
     * If the socket was bound prior to being closed, then this method will continue to return the port number after the
     * socket is closed.
     *
     * @return the port number to which this socket is listening, or -1 if the socket is unbound.
     *
     * @see java.net.ServerSocket.getLocalPort
     * @see java.nio.channels.AsynchronousServerSocketChannel.getLocalAddress
     */
    suspend fun getLocalPort(): Int

    /**
     * Returns the address of the endpoint this socket is bound to.
     *
     * If the socket was bound prior to being closed, then this method will continue to return the address of the
     * endpoint after the socket is closed.
     *
     * If there is a security manager set, its checkConnect method is called with the local address and -1 as its
     * arguments to see if the operation is allowed. If the operation is not allowed, a SocketAddress representing the
     * loopback address and the local port to which the socket is bound is returned.
     *
     * @return a SocketAddress representing the local endpoint of this socket, or a SocketAddress representing the
     * loopback address if denied by the security manager, or `null` if the socket is not bound yet.
     *
     * @see java.net.ServerSocket.getLocalSocketAddress
     * @see java.nio.channels.AsynchronousServerSocketChannel.getLocalAddress
     */
    suspend fun getLocalSocketAddress(): SocketAddress?

    /**
     * Enable/disable the [SO_REUSEADDR] socket option.
     *
     * When a TCP connection is closed the connection may remain in a timeout state for a period of time after the
     * connection is closed (typically known as the TIME_WAIT state or 2MSL wait state). For applications using a well
     * known socket address or port it may not be possible to bind a socket to the required SocketAddress if there is a
     * connection in the timeout state involving the socket address or port.
     *
     * Enabling SO_REUSEADDR prior to binding the socket using bind(SocketAddress) allows the socket to be bound even
     * though a previous connection is in a timeout state.
     *
     * When a ServerSocket is created the initial setting of [SO_REUSEADDR] is not defined. Applications can use
     * getReuseAddress() to determine the initial setting of [SO_REUSEADDR].
     *
     * The behaviour when SO_REUSEADDR is enabled or disabled after a socket is bound (See isBound()) is not defined.
     *
     * @param on `true` to enable [SO_REUSEADDR], `false` to disable.
     *
     * @throws SocketException if an I/O error occurs when setting the option.
     *
     * @see java.net.ServerSocket.setReuseAddress
     * @see java.nio.channels.AsynchronousServerSocketChannel.setOption
     * @see SO_REUSEADDR
     */
    @Throws(SocketException::class)
    suspend fun setReuseAddress(on: Boolean)

    /**
     * Tests if [SO_REUSEADDR] is enabled.
     *
     * @return `true` if [SO_REUSEADDR] is enabled, `false` if it is disabled.
     *
     * @throws SocketException if an I/O error occurs when getting the option.
     *
     * @see java.net.ServerSocket.getReuseAddress
     * @see java.nio.channels.AsynchronousServerSocketChannel.setOption
     * @see SO_REUSEADDR
     */
    @Throws(SocketException::class)
    suspend fun getReuseAddress(): Boolean

    /**
     * Sets a default proposed value for the [SO_RCVBUF] option for sockets accepted from this ServerSocket. The value
     * actually set in the accepted socket must be determined by calling [KSocket.getReceiveBufferSize] after the
     * socket is returned by [accept].
     *
     * The value of [SO_RCVBUF] is used both to set the size of the internal socket receive buffer, and to set the size
     * of the TCP receive window that is advertized to the remote peer.
     *
     * It is possible to change the value subsequently, by calling [KSocket.setReceiveBufferSize]. However, if the
     * application wishes to allow a receive window larger than 64K bytes, as defined by RFC1323 then the proposed value
     * must be set in the ServerSocket before it is bound to a local address. This implies, that the ServerSocket must
     * be created with the no-argument constructor, then [setReceiveBufferSize] must be called and lastly the
     * [KServerSocket] is bound to an address by calling [bind].
     *
     * Failure to do this will not cause an error, and the buffer size may be set to the requested value but the TCP
     * receive window in sockets accepted from this ServerSocket will be no larger than 64K bytes.
     *
     * @param size the size to which to set the receive buffer size. This value must be greater than 0.
     *
     * @throws SocketException if an I/O error occurs when setting the option.
     *
     * @see java.net.ServerSocket.setReceiveBufferSize
     * @see java.nio.channels.AsynchronousServerSocketChannel.setOption
     * @see SO_RCVBUF
     */
    @Throws(SocketException::class)
    suspend fun setReceiveBufferSize(size: Int)


    /**
     * Gets the value of the [SO_RCVBUF] option for this ServerSocket, that is the proposed buffer size that will be
     * used for [KSocket]s accepted from this [KServerSocket].
     *
     * Note, the value actually set in the accepted socket is determined by calling [KSocket.getReceiveBufferSize].
     *
     * @return the value of the [SO_RCVBUF] option for this [KServerSocket].
     *
     * @throws SocketException if an I/O error occurs when getting the option.
     *
     * @see java.net.ServerSocket.getReceiveBufferSize
     * @see java.nio.channels.AsynchronousServerSocketChannel.getOption
     * @see SO_RCVBUF
     */
    @Throws(SocketException::class)
    suspend fun getReceiveBufferSize(): Int

    /**
     * Returns the binding state of the [KServerSocket].
     *
     * @return `true` if the ServerSocket is bound to an address, `false` otherwise.
     *
     * @see java.net.ServerSocket.isBound
     * @see java.nio.channels.AsynchronousServerSocketChannel.getLocalAddress
     */
    suspend fun isBound(): Boolean

    /**
     * Returns the closed state of the [KServerSocket].
     *
     * @return `true` if the [KServerSocket] is closed, `false` otherwise.
     *
     * @see java.net.ServerSocket.isClosed
     * @see java.nio.channels.AsynchronousServerSocketChannel.isOpen
     */
    suspend fun isClosed(): Boolean
}