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
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * A coroutine-based socket implementation using AsynchronousSocketChannel.
 */
class KSocket internal constructor(channel: AsynchronousSocketChannel = AsynchronousSocketChannel.open()) : KAutoCloseable {

    companion object {
        /**
         * Opens a new KSocket and connects it to the specified address.
         *
         * @param address The address to connect to.
         * @return A connected KSocket instance.
         */
        suspend fun open(address: SocketAddress): KSocket {
            return KSocket().connect(address)
        }

        /**
         * Handles asynchronous failures.
         *
         * @param e The throwable to handle.
         * @return The result of the failure handling.
         */
        private fun <T> handleAyncFailure(e: Throwable): T {
            throw if (e is InterruptedByTimeoutException) {
                SocketTimeoutException("Connection timed out")
            } else {
                e
            }
        }
    }

    /** The underlying AsynchronousSocketChannel. */
    private val channel = channel

    /** The read mutex for synchronization. */
    private val readMutex = Mutex()
    /** The read timeout in milliseconds. */
    private var readTimeout: Long? = null
    /** The input shutdown flag. */
    private var isInputShutdown: Boolean = false

    /** The write mutex for synchronization. */
    private val writeMutex = Mutex()
    /** The write timeout in milliseconds. */
    private var writeTimeout: Long? = null
    /** The output shutdown flag. */
    private var isOutputShutdown: Boolean = false

    /** The input stream of the socket. */
    private val inputStream = object : KInputStream() {
        /**
         * Reads data into the provided ByteBuffer.
         *
         * @param b The ByteBuffer to read data into.
         * @return The number of bytes read.
         */
        override suspend fun read(b: ByteBuffer): Int = readMutex.withLock {
            val result = read0(b)
            if (result == -1) {
                close()
            }

            result
        }

        /**
         * Performs the actual read operation.
         *
         * @param b The ByteBuffer to read data into.
         * @return The number of bytes read.
         */
        suspend fun read0 (b: ByteBuffer): Int = suspendCoroutine { co ->
            try {
                val handler = fromResult<Int> (onFail=::handleAyncFailure)

                if (readTimeout != null) {
                    channel.read(b, readTimeout!!, TimeUnit.MILLISECONDS, co, handler)
                } else {
                    channel.read(b, co, handler)
                }
            } catch (e: Throwable) {
                co.resumeWithException(e)
            }
        }

        /**
         * Closes the input stream.
         */
        override suspend fun close() = this@KSocket.shutdownInput()
    }

    /** The output stream of the socket. */
    private val outputStream = object : KOutputStream() {
        /**
         * Writes data from the provided ByteBuffer.
         *
         * @param b The ByteBuffer containing data to write.
         */
        override suspend fun write(b: ByteBuffer):Unit = writeMutex.withLock {
            while(b.hasRemaining()) {
                val result = write0(b)
                if (result == -1) {
                    close()
                    break
                }
            }
        }

        /**
         * Performs the actual write operation.
         *
         * @param b The ByteBuffer containing data to write.
         */
        suspend fun write0(b: ByteBuffer): Int = suspendCoroutine { co ->
            try {
                val handler = fromResult<Int> (onFail=::handleAyncFailure)

                if(writeTimeout!=null) {
                    channel.write(b, writeTimeout!!, TimeUnit.MILLISECONDS, co, handler)
                } else {
                    channel.write(b, co, handler)
                }
            } catch (e: Throwable) {
                co.resumeWithException(e)
            }
        }

        /**
         * Closes the output stream.
         */
        override suspend fun close() = this@KSocket.shutdownOutput()
    }

    /**
     * Binds the socket to a local address.
     * If the address is null, then the system will pick up an ephemeral port and a valid local address to bind the socket.
     *
     * @param local the SocketAddress to bind to
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun bind(local: SocketAddress? = null) {
        channel.bind(local)
    }

    /**
     * Closes the socket.
     */
    override suspend fun close() = withContext(Dispatchers.IO) {
        if (!channel.isOpen) return@withContext
        try {
            channel.close()
        } catch (e: IOException) {
            // Log or handle the close exception if necessary
        }
    }

    /**
     * Connects this channel.
     *
     * @param endpoint The address to connect to
     * @param timeout The timeout in milliseconds, or 0 for no timeout
     * @throws IOException if an I/O error occurs
     */
    suspend fun connect(endpoint: SocketAddress, timeout: Long = 0): KSocket = suspendCoroutine {
        try {
            val timoutJob = if (timeout > 0) {
                it.timeout(timeout) { SocketTimeoutException("Connection timed out") }
            } else if (timeout<0) {
                throw IllegalArgumentException("Timeout must be greater than or equal to 0")
            } else {
                null
            }

            // returns "this" upon completion
            channel.connect(endpoint, it, this.asCompletionHandler(timoutJob))
        } catch (e: Throwable) {
            it.resumeWithException(e)
        }
    }

    /**
     * Gets the underlying AsynchronousSocketChannel.
     *
     * @return The AsynchronousSocketChannel.
     */
    fun getChannel(): AsynchronousSocketChannel = channel

    /**
     * Gets the remote InetAddress.
     *
     * @return The remote InetAddress, or null if not connected.
     */
    fun getInetAddress(): java.net.InetAddress? {
        val address = channel.remoteAddress ?: return null
        return if(address is java.net.InetSocketAddress) {
            address.address
        } else {
            null
        }
    }

    /**
     * Gets the input stream of the socket.
     *
     * @return The KInputStream.
     */
    fun getInputStream(): KInputStream = this.inputStream

    /**
     * Gets the SO_KEEPALIVE option.
     *
     * @return The value of the SO_KEEPALIVE option.
     */
    fun getKeepAlive(): Boolean = channel.getOption(java.net.StandardSocketOptions.SO_KEEPALIVE)

    /**
     * Gets the local InetAddress.
     *
     * @return The local InetAddress, or null if not bound.
     */
    fun getLocalAddress(): java.net.InetAddress? {
        val address = channel.localAddress ?: null
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
    fun getLocalPort(): Int {
        val address = channel.localAddress ?: return -1
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
    fun getLocalSocketAddress(): SocketAddress? = channel.localAddress

    /**
     * Gets the output stream of the socket.
     *
     * @return The KOutputStream.
     */
    fun getOutputStream(): KOutputStream = outputStream

    /**
     * Gets the remote port number.
     *
     * @return The remote port number, or -1 if not connected.
     */
    fun getPort(): Int {
        val address = channel.remoteAddress ?: return -1
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
    fun getReceiveBufferSize(): Int = channel.getOption(java.net.StandardSocketOptions.SO_RCVBUF)

    /**
     * Gets the remote socket address.
     *
     * @return The remote SocketAddress.
     */
    fun getRemoteSocketAddress(): SocketAddress = channel.remoteAddress

    /**
     * Gets the SO_REUSEADDR option.
     *
     * @return The value of the SO_REUSEADDR option.
     */
    fun getReuseAddress(): Boolean = channel.getOption(java.net.StandardSocketOptions.SO_REUSEADDR)

    /**
     * Gets the SO_SNDBUF option.
     *
     * @return The value of the SO_SNDBUF option.
     */
    fun getSendBufferSize(): Int = channel.getOption(java.net.StandardSocketOptions.SO_SNDBUF)

    /**
     * Gets the SO_LINGER option.
     *
     * @return The value of the SO_LINGER option.
     */
    fun getSoLinger(): Int = channel.getOption(java.net.StandardSocketOptions.SO_LINGER)

    /**
     * Gets the read timeout.
     *
     * @return The read timeout in milliseconds.
     */
    fun getReadTimeout(): Long = this.readTimeout ?: 0

    /**
     * Gets the write timeout.
     *
     * @return The write timeout in milliseconds.
     */
    fun getWriteTimeout(): Long = this.writeTimeout ?: 0

    /**
     * Gets the TCP_NODELAY option.
     *
     * @return The value of the TCP_NODELAY option.
     */
    fun getTcpNoDelay(): Boolean = channel.getOption(java.net.StandardSocketOptions.TCP_NODELAY)

    /**
     * Gets the IP_TOS option.
     *
     * @return The value of the IP_TOS option.
     */
    fun getTrafficClass(): Int = channel.getOption(java.net.StandardSocketOptions.IP_TOS)

    /**
     * Checks if the socket is bound.
     *
     * @return True if the socket is bound, false otherwise.
     */
    fun isBound(): Boolean = channel.localAddress != null

    /**
     * Checks if the socket is closed.
     *
     * @return True if the socket is closed, false otherwise.
     */
    fun isClosed(): Boolean = !channel.isOpen

    /**
     * Checks if the socket is connected.
     *
     * @return True if the socket is connected, false otherwise.
     */
    fun isConnected(): Boolean = channel.remoteAddress != null

    /**
     * Checks if the input is shutdown.
     *
     * @return True if the input is shutdown, false otherwise.
     */
    suspend fun isInputShutdown(): Boolean = readMutex.withLock { isInputShutdown }

    /**
     * Checks if the output is shutdown.
     *
     * @return True if the output is shutdown, false otherwise.
     */
    suspend fun isOutputShutdown(): Boolean = writeMutex.withLock { isOutputShutdown }

    /**
     * Sets the SO_KEEPALIVE option.
     *
     * @param keepAlive The value to set.
     */
    @Throws(IOException::class)
    suspend fun setKeepAlive(keepAlive: Boolean) = withContext(Dispatchers.IO) {
        channel.setOption(java.net.StandardSocketOptions.SO_KEEPALIVE, keepAlive)
    }

    /**
     * Sets the SO_RCVBUF option.
     *
     * @param size The buffer size to set.
     */
    @Throws(IOException::class)
    suspend fun setReceiveBufferSize(size: Int) = withContext(Dispatchers.IO) {
        channel.setOption(java.net.StandardSocketOptions.SO_RCVBUF, size)
    }

    /**
     * Sets the SO_REUSEADDR option.
     *
     * @param reuse The value to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun setReuseAddress(reuse: Boolean):Unit = withContext(Dispatchers.IO) {
        channel.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, reuse)
    }

    /**
     * Sets the SO_SNDBUF option.
     *
     * @param size The buffer size to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun setSendBufferSize(size: Int):Unit = withContext(Dispatchers.IO) {
        channel.setOption(java.net.StandardSocketOptions.SO_SNDBUF, size)
    }

    /**
     * Sets the read timeout.
     *
     * @param timeout The timeout in milliseconds, or null to disable.
     */
    fun setReadTimeout(timeout: Long?) {
        if(timeout==null || timeout==0L) {
            this.readTimeout = null
        } else if(timeout<0) {
            throw IllegalArgumentException("Timeout must be greater than or equal to 0")
        } else {
            this.readTimeout = timeout
        }
    }

    /**
     * Sets the write timeout.
     *
     * @param timeout The timeout in milliseconds, or null to disable.
     */
    fun setWriteTimeout(timeout: Long?) {
        if(timeout==null || timeout==0L) {
            this.writeTimeout = null
        } else if(timeout<0) {
            throw IllegalArgumentException("Timeout must be greater than or equal to 0")
        } else {
            this.writeTimeout = timeout
        }
    }

    /**
     * Sets the TCP_NODELAY option.
     *
     * @param on The value to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun setTcpNoDelay(on: Boolean):Unit = withContext(Dispatchers.IO) {
        channel.setOption(java.net.StandardSocketOptions.TCP_NODELAY, on)
    }

    /**
     * Sets the IP_TOS option.
     *
     * @param tc The value to set.
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun setTrafficClass(tc: Int):Unit = withContext(Dispatchers.IO) {
        channel.setOption(java.net.StandardSocketOptions.IP_TOS, tc)
    }

    /**
     * Shuts down the input side of the socket.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun shutdownInput():Unit = withContext(Dispatchers.IO) {
        readMutex.withLock {
            isInputShutdown = true
            channel.shutdownInput()
        }
    }

    /**
     * Shuts down the output side of the socket.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    suspend fun shutdownOutput():Unit = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            isOutputShutdown = true
            channel.shutdownOutput()
        }
    }
}