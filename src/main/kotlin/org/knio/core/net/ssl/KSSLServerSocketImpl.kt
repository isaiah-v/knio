package org.knio.core.net.ssl

import org.knio.core.nio.acceptSuspend
import org.knio.core.context.KnioContext
import java.net.SocketAddress
import javax.net.ssl.SSLContext

internal class KSSLServerSocketImpl (
    sslContext: SSLContext,
    private val context: KnioContext
): KSSLServerSocketAbstract(
    sslContext,
    context.channelFactory.openServerSocketChannel()
) {

    private var acceptTimeout: Long? = null

    override suspend fun accept(): KSSLSocket {
        val channel = serverChannel.acceptSuspend(acceptTimeout)

        return KSSLSocketImpl(
            channel = channel,
            sslEngine = createSSLEngine(),
            useClientMode = isUseClientMode,
            context = context
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