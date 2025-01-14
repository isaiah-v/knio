package org.ivcode.knio.net

import org.ivcode.knio.context.getKnioContext
import java.io.IOException
import java.net.InetAddress

interface KServerSocketFactory {

    companion object {
        suspend fun getDefault(): KServerSocketFactory {
            return KServerSocketFactoryDefault(getKnioContext())
        }
    }

    @Throws(IOException::class)
    suspend fun createServerSocket(): KServerSocket

    @Throws(IOException::class)
    suspend fun createServerSocket(port: Int): KServerSocket

    @Throws(IOException::class)
    suspend fun createServerSocket(port: Int, backlog: Int): KServerSocket

    @Throws(IOException::class)
    suspend fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress): KServerSocket
}