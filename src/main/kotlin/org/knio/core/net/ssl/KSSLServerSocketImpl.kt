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

    override suspend fun accept(): KSSLSocket {
        val channel = serverChannel.acceptSuspend()

        return KSSLSocketImpl(
            channel = channel,
            sslEngine = createSSLEngine(),
            useClientMode = getUseClientMode(),
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

}