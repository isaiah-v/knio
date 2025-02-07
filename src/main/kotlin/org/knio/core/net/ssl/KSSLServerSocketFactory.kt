package org.knio.core.net.ssl

import org.knio.core.net.KServerSocketFactory
import org.knio.core.context.getKnioContext
import java.net.InetAddress
import javax.net.ssl.SSLContext

interface KSSLServerSocketFactory: KServerSocketFactory {

    companion object {

        suspend fun getDefault(): KSSLServerSocketFactory {
            val context = getKnioContext()

            @Suppress("BlockingMethodInNonBlockingContext")
            return KSSLServerSocketFactoryDefault(SSLContext.getDefault(), context)
        }
    }

    override suspend fun createServerSocket(): KSSLServerSocket
    override suspend fun createServerSocket(port: Int): KSSLServerSocket
    override suspend fun createServerSocket(port: Int, backlog: Int): KSSLServerSocket
    override suspend fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress): KSSLServerSocket
}