package org.ivcode.knio.net

import org.ivcode.knio.context.KnioContext
import org.ivcode.knio.context.getKnioContext
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException

interface KSocketFactory {
    companion object {
        suspend fun getDefault(): KSocketFactory {
            return DefaultKSocketFactory(getKnioContext())
        }
    }

    @Throws(IOException::class)
    suspend fun createSocket(): KSocket

    @Throws(IOException::class, UnknownHostException::class)
    suspend fun createSocket(host: String, port: Int): KSocket

    @Throws(IOException::class, UnknownHostException::class)
    suspend fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): KSocket

    @Throws(IOException::class)
    suspend fun createSocket(host: InetAddress, port: Int): KSocket

    @Throws(IOException::class)
    suspend fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): KSocket

    private class DefaultKSocketFactory(
        private val context: KnioContext
    ): KSocketFactory {
        override suspend fun createSocket() = KSocketImpl(
            channel = context.channelFactory.openSocketChannel(),
            context = context
        )

        override suspend fun createSocket(host: String, port: Int) =
            createSocket().apply { connect(InetSocketAddress(host, port)) }

        override suspend fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int) = createSocket().apply {
            connect(InetSocketAddress(host, port))
            bind(InetSocketAddress(localHost, localPort))
        }

        override suspend fun createSocket(host: InetAddress, port: Int) = createSocket().apply {
            connect(InetSocketAddress(host, port))
        }

        override suspend fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int) = createSocket().apply {
            connect(InetSocketAddress(address, port))
            bind(InetSocketAddress(localAddress, localPort))
        }
    }
}