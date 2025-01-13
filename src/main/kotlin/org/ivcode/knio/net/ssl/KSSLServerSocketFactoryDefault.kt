package org.ivcode.knio.net.ssl

import org.ivcode.knio.context.KnioContext
import org.jetbrains.annotations.Blocking
import java.net.InetAddress
import java.net.InetSocketAddress
import javax.net.ssl.SSLContext

@Blocking
class KSSLServerSocketFactoryDefault(
    private val sslContext: SSLContext,
    private val context: KnioContext
): KSSLServerSocketFactory {

    override suspend fun createServerSocket(): KSSLServerSocket {
        @Suppress("BlockingMethodInNonBlockingContext")
        return KSSLServerSocketImpl (
            sslContext = sslContext,
            context = context
        )
    }

    override suspend fun createServerSocket(port: Int): KSSLServerSocket {
        @Suppress("BlockingMethodInNonBlockingContext")
        return createServerSocket().apply { bind(InetSocketAddress(port)) }
    }

    override suspend fun createServerSocket(port: Int, backlog: Int): KSSLServerSocket {
        @Suppress("BlockingMethodInNonBlockingContext")
        return createServerSocket().apply { bind(InetSocketAddress(port), backlog) }
    }

    override suspend fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress): KSSLServerSocket {
        @Suppress("BlockingMethodInNonBlockingContext")
        return createServerSocket().apply { bind(InetSocketAddress(ifAddress, port), backlog) }
    }
}