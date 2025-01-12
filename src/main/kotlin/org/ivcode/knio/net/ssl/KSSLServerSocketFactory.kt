package org.ivcode.knio.net.ssl

import org.ivcode.knio.net.KServerSocketFactory
import java.net.InetAddress
import javax.net.ssl.SSLContext

interface KSSLServerSocketFactory: KServerSocketFactory {

    companion object {
        private val DEFAULT: KSSLServerSocketFactory = KSSLServerSocketFactoryDefault(SSLContext.getDefault())

        fun getDefault(): KSSLServerSocketFactory {
            return DEFAULT
        }
    }

    override suspend fun createServerSocket(): KSSLServerSocket
    override suspend fun createServerSocket(port: Int): KSSLServerSocket
    override suspend fun createServerSocket(port: Int, backlog: Int): KSSLServerSocket
    override suspend fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress): KSSLServerSocket
}