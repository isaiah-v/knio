package org.knio.core.test.servers.accept

import org.knio.core.test.servers.TestServer
import java.lang.Thread.sleep
import java.net.ServerSocket
import java.net.SocketException
import javax.net.ssl.SSLSocket

class AcceptOnlyServer(
    private val serverSocket: ServerSocket
): TestServer, Runnable {

    override fun run() {
        while(!serverSocket.isClosed) {
            try {
                val socket = serverSocket.accept()
                if(socket is SSLSocket) {
                    socket.startHandshake()
                }

                Thread {
                    sleep(10000)
                    socket.close()
                }.start()
            } catch (e: SocketException) {
                // socket closed
            }
        }
    }

    override suspend fun start(): TestServer {
        Thread(this).start()
        return this
    }

    override suspend fun stop() {
        serverSocket.close()
    }
}