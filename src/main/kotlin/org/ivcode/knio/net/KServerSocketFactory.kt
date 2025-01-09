package org.ivcode.knio.net

import org.ivcode.org.ivcode.knio.net.KServerSocketFactoryDefault
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress


interface KServerSocketFactory {

    companion object {
        fun getDefault(): KServerSocketFactory {
            return KServerSocketFactoryDefault()
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