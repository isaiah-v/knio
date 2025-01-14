package org.ivcode.knio.net.ssl

import org.ivcode.knio.net.KServerSocketFactory
import org.ivcode.knio.context.getKnioContext
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