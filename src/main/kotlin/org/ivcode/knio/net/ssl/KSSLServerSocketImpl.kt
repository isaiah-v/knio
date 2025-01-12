package org.ivcode.knio.net.ssl

import org.ivcode.knio.system.ByteBufferPool
import org.ivcode.knio.nio.acceptSuspend
import org.jetbrains.annotations.Blocking
import java.net.SocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import javax.net.ssl.SSLContext

@Blocking
internal class KSSLServerSocketImpl (
    sslContext: SSLContext,
    serverChannel: AsynchronousServerSocketChannel,
    private val bufferPool: ByteBufferPool = ByteBufferPool.getDefault()
): KSSLServerSocketAbstract(
    sslContext,
    serverChannel
) {

    private var acceptTimeout: Long? = null

    override suspend fun accept(): KSSLSocket {
        val channel = serverChannel.acceptSuspend(acceptTimeout)

        return KSSLSocketImpl(
            channel = channel,
            sslEngine = @Suppress("BlockingMethodInNonBlockingContext") createSSLEngine(),
            useClientMode = isUseClientMode,
            bufferPool
        )
    }

    override suspend fun bind(endpoint: SocketAddress, backlog: Int) {
        @Suppress("BlockingMethodInNonBlockingContext")
        serverChannel.bind(endpoint, backlog)
    }

    override suspend fun close() {
        @Suppress("BlockingMethodInNonBlockingContext")
        serverChannel.close()
    }

    override suspend fun setAcceptTimeout(timeout: Long) {
        acceptTimeout = if (timeout < 0) {
            throw IllegalArgumentException("timeout value is negative")
        } else if (timeout == 0L) {
            null
        } else {
            timeout
        }
    }

    override suspend fun getAcceptTimeout(): Long {
        return acceptTimeout ?: 0
    }
}