package org.ivcode.knio.net.ssl

import org.ivcode.knio.nio.acceptSuspend
import org.ivcode.knio.system.KnioContext
import org.jetbrains.annotations.Blocking
import java.net.SocketAddress
import javax.net.ssl.SSLContext

@Blocking
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
            sslEngine = @Suppress("BlockingMethodInNonBlockingContext") createSSLEngine(),
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