package org.ivcode.knio.net.ssl

import org.ivcode.knio.system.ByteBufferPool
import org.ivcode.knio.system.ChannelFactory
import org.jetbrains.annotations.Blocking
import java.net.InetAddress
import java.net.InetSocketAddress
import javax.net.ssl.SSLContext

@Blocking
class KSSLServerSocketFactoryDefault(
    private val sslContext: SSLContext,
    private val channelFactory: ChannelFactory = ChannelFactory.getDefault(),
    private val bufferPool: ByteBufferPool = ByteBufferPool.getDefault()
): KSSLServerSocketFactory {

    override suspend fun createServerSocket(): KSSLServerSocket {
        @Suppress("BlockingMethodInNonBlockingContext")
        return KSSLServerSocketImpl (
            serverChannel = channelFactory.openServerSocketChannel(),
            sslContext = sslContext,
            bufferPool = bufferPool
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