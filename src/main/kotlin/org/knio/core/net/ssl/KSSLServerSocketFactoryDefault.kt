package org.knio.core.net.ssl

import org.knio.core.context.KnioContext
import java.net.InetAddress
import java.net.InetSocketAddress
import javax.net.ssl.SSLContext

class KSSLServerSocketFactoryDefault(
    private val sslContext: SSLContext,
    private val context: KnioContext
): KSSLServerSocketFactory {

    override suspend fun createServerSocket(): KSSLServerSocket {
        return KSSLServerSocketImpl (
            sslContext = sslContext,
            context = context
        )
    }

    override suspend fun createServerSocket(port: Int): KSSLServerSocket {
        return createServerSocket().apply { bind(InetSocketAddress(port)) }
    }

    override suspend fun createServerSocket(port: Int, backlog: Int): KSSLServerSocket {
        return createServerSocket().apply { bind(InetSocketAddress(port), backlog) }
    }

    override suspend fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress): KSSLServerSocket {
        return createServerSocket().apply { bind(InetSocketAddress(ifAddress, port), backlog) }
    }
}