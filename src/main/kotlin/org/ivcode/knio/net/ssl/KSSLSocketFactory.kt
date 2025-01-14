package org.ivcode.knio.net.ssl

import org.ivcode.knio.net.KSocketFactory
import org.ivcode.knio.context.getKnioContext
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import javax.net.ssl.SSLContext
import kotlin.jvm.Throws

interface KSSLSocketFactory: KSocketFactory {

    companion object {

        suspend fun getDefault(): KSSLSocketFactory {
            return KSSLSocketFactoryDefault(
                sslContext = SSLContext.getDefault(),
                context = getKnioContext()
            )
        }
    }

    @Throws(IOException::class)
    override suspend fun createSocket(): KSSLSocket

    @Throws(IOException::class, UnknownHostException::class)
    override suspend fun createSocket(host: String, port: Int): KSSLSocket

    @Throws(IOException::class, UnknownHostException::class)
    override suspend fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): KSSLSocket

    @Throws(IOException::class)
    override suspend fun createSocket(host: InetAddress, port: Int): KSSLSocket

    @Throws(IOException::class)
    override suspend fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): KSSLSocket

    @Throws(IOException::class)
    suspend fun getDefaultCipherSuites(): Array<String>

    @Throws(IOException::class)
    suspend fun getSupportedCipherSuites(): Array<String>
}
