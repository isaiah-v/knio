package org.ivcode.knio.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ivcode.knio.io.KAutoCloseable
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

class KSocket: KAutoCloseable {

    companion object {
        suspend fun open(address: SocketAddress): KSocket {
            return KSocket().connect(address)
        }

        private fun <T> handleAyncFailure(e: Throwable): T {
            throw if (e is InterruptedByTimeoutException) {
                SocketTimeoutException("Connection timed out")
            } else {
                e
            }
        }
    }

    private val channel = AsynchronousSocketChannel.open()

    private val readMutex = Mutex()
    private var readTimeout: Long? = null
    private var isInputShutdown: Boolean = false

    private val writeMutex = Mutex()
    private var writeTimeout: Long? = null
    private var isOutputShutdown: Boolean = false

    private val inputStream = object : KInputStream() {
        override suspend fun read(b: ByteBuffer): Int = readMutex.withLock {
            return read0(b)
        }

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

        override suspend fun close() = this@KSocket.shutdownInput()
    }

    private val outputStream = object : KOutputStream() {
        override suspend fun write(b: ByteBuffer):Unit = writeMutex.withLock {
            return write0(b)
        }

        suspend fun write0(b: ByteBuffer): Unit = suspendCoroutine { co ->
            try {
                val handler = Unit.asCompletionHandler<Int,Unit> (onFail=::handleAyncFailure)

                if(writeTimeout!=null) {
                    channel.write(b, writeTimeout!!, TimeUnit.MILLISECONDS, co, handler)
                } else {
                    channel.write(b, co, handler)
                }
            } catch (e: Throwable) {
                co.resumeWithException(e)
            }
        }

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

    fun getChannel(): AsynchronousSocketChannel = channel
    fun getInetAddress(): java.net.InetAddress? {
        val address = channel.remoteAddress ?: return null
        return if(address is java.net.InetSocketAddress) {
            address.address
        } else {
            null
        }

    }
    fun getInputStream(): KInputStream = this.inputStream

    fun getKeepAlive(): Boolean = channel.getOption(java.net.StandardSocketOptions.SO_KEEPALIVE)

    fun getLocalAddress(): java.net.InetAddress? {
        val address = channel.localAddress ?: null
        return if(address is java.net.InetSocketAddress) {
            address.address
        } else {
            null
        }
    }

    fun getLocalPort(): Int {
        val address = channel.localAddress ?: return -1
        return if(address is java.net.InetSocketAddress) {
            address.port
        } else {
            -1
        }
    }

    fun getLocalSocketAddress(): SocketAddress? = channel.localAddress
    fun getOutputStream(): KOutputStream = outputStream
    fun getPort(): Int {
        val address = channel.remoteAddress ?: return -1
        return if(address is java.net.InetSocketAddress) {
            address.port
        } else {
            -1
        }
    }
    fun getReceiveBufferSize(): Int = channel.getOption(java.net.StandardSocketOptions.SO_RCVBUF)
    fun getRemoteSocketAddress(): SocketAddress = channel.remoteAddress
    fun getReuseAddress(): Boolean = channel.getOption(java.net.StandardSocketOptions.SO_REUSEADDR)
    fun getSendBufferSize(): Int = channel.getOption(java.net.StandardSocketOptions.SO_SNDBUF)
    fun getSoLinger(): Int = channel.getOption(java.net.StandardSocketOptions.SO_LINGER)
    fun getReadTimeout(): Long = this.readTimeout ?: 0
    fun getWriteTimeout(): Long = this.writeTimeout ?: 0
    fun getTcpNoDelay(): Boolean = channel.getOption(java.net.StandardSocketOptions.TCP_NODELAY)
    fun getTrafficClass(): Int = channel.getOption(java.net.StandardSocketOptions.IP_TOS)
    fun isBound(): Boolean = channel.localAddress != null
    fun isClosed(): Boolean = !channel.isOpen
    fun isConnected(): Boolean = channel.remoteAddress != null
    suspend fun isInputShutdown(): Boolean = readMutex.withLock { isInputShutdown }
    suspend fun isOutputShutdown(): Boolean = writeMutex.withLock { isOutputShutdown }
    fun setKeepAlive(keepAlive: Boolean) {
        channel.setOption(java.net.StandardSocketOptions.SO_KEEPALIVE, keepAlive)
    }

    fun setReceiveBufferSize(size: Int) {
        channel.setOption(java.net.StandardSocketOptions.SO_RCVBUF, size)
    }

    @Throws(IOException::class)
    suspend fun setReuseAddress(reuse: Boolean):Unit = withContext(Dispatchers.IO) {
        channel.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, reuse)
    }

    @Throws(IOException::class)
    suspend fun setSendBufferSize(size: Int):Unit = withContext(Dispatchers.IO) {
        channel.setOption(java.net.StandardSocketOptions.SO_SNDBUF, size)
    }

    fun setReadTimeout(timeout: Long?) {
        if(timeout==null || timeout==0L) {
            this.readTimeout = null
        } else if(timeout<0) {
            throw IllegalArgumentException("Timeout must be greater than or equal to 0")
        } else {
            this.readTimeout = timeout
        }
    }
    fun setWriteTimeout(timeout: Long?) {
        if(timeout==null || timeout==0L) {
            this.writeTimeout = null
        } else if(timeout<0) {
            throw IllegalArgumentException("Timeout must be greater than or equal to 0")
        } else {
            this.writeTimeout = timeout
        }
    }

    @Throws(IOException::class)
    suspend fun setTcpNoDelay(on: Boolean):Unit = withContext(Dispatchers.IO) {
        channel.setOption(java.net.StandardSocketOptions.TCP_NODELAY, on)
    }

    @Throws(IOException::class)
    suspend fun setTrafficClass(tc: Int):Unit = withContext(Dispatchers.IO) {
        channel.setOption(java.net.StandardSocketOptions.IP_TOS, tc)
    }

    @Throws(IOException::class)
    suspend fun shutdownInput():Unit = withContext(Dispatchers.IO) {
        readMutex.withLock {
            isInputShutdown = true
            channel.shutdownInput()
        }
    }

    @Throws(IOException::class)
    suspend fun shutdownOutput():Unit = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            isOutputShutdown = true
            channel.shutdownOutput()
        }
    }
}