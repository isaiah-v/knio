package org.ivcode.knio.net

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress


interface KServerSocketFactory {

    companion object {
        fun getDefault(): KServerSocketFactory {
            return DefaultKSocketServerFactory()
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

    private class DefaultKSocketServerFactory: KServerSocketFactory {
        override suspend fun createServerSocket() =
            KServerSocketImpl()

        override suspend fun createServerSocket(port: Int) =
            KServerSocketImpl().open(port)

        override suspend fun createServerSocket(port: Int, backlog: Int) =
            KServerSocketImpl().open(port, backlog)

        override suspend fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress) =
            KServerSocketImpl().open(port, backlog, ifAddress)

        private suspend fun KServerSocket.open(port: Int, backlog: Int = 0, bindAddress: InetAddress? = null): KServerSocket {
            try {
                require(port in 1..0xfffe) { "Port value out of range: $port" }

                val bkLog = if (backlog < 1) 50 else backlog
                val local = InetSocketAddress(bindAddress, port)

                bind(local, bkLog)
            } catch  (th: Throwable) {
                close()
            }

            return this
        }
    }
}