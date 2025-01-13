package org.ivcode.knio.net.ssl

import org.ivcode.knio.context.KnioContext
import java.net.InetAddress
import java.net.InetSocketAddress
import javax.net.ssl.SSLContext

internal class KSSLSocketFactoryDefault(
    private val sslContext: SSLContext,
    private val context: KnioContext
): KSSLSocketFactory {
    override suspend fun createSocket(): KSSLSocket {

        return KSSLSocketImpl (
            channel = context.channelFactory.openSocketChannel(),
            sslEngine = sslContext.createSSLEngine(),
            useClientMode = true,
            context = context
        )
    }

    override suspend fun createSocket(host: String, port: Int) = createSocket().apply {
        connect(InetSocketAddress(host, port))
    }

    override suspend fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int
    ) = createSocket().apply {
        connect(InetSocketAddress(host, port))
        bind(InetSocketAddress(localHost, localPort))
    }

    override suspend fun createSocket(host: InetAddress, port: Int) = createSocket().apply {
        connect(InetSocketAddress(host, port))
    }

    override suspend fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int
    ) = createSocket().apply {
        connect(InetSocketAddress(address, port))
        bind(InetSocketAddress(localAddress, localPort))
    }

    override suspend fun getDefaultCipherSuites(): Array<String> = sslContext.defaultSSLParameters.cipherSuites

    override suspend fun getSupportedCipherSuites(): Array<String> = sslContext.supportedSSLParameters.cipherSuites
}