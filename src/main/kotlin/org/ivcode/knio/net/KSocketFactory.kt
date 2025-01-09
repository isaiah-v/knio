package org.ivcode.knio.net

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException


interface KSocketFactory {
    companion object {
        fun getDefault(): KSocketFactory {
            return DefaultKSocketFactory()
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

    private class DefaultKSocketFactory: KSocketFactory {
        override suspend fun createSocket() = KSocketImpl()

        override suspend fun createSocket(host: String, port: Int) =
            KSocketImpl().apply { connect(InetSocketAddress(host, port)) }

        override suspend fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int) = KSocketImpl().apply {
            connect(InetSocketAddress(host, port))
            bind(InetSocketAddress(localHost, localPort))
        }

        override suspend fun createSocket(host: InetAddress, port: Int) = KSocketImpl().apply {
            connect(InetSocketAddress(host, port))
        }

        override suspend fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int) = KSocketImpl().apply {
            connect(InetSocketAddress(address, port))
            bind(InetSocketAddress(localAddress, localPort))
        }
    }
}