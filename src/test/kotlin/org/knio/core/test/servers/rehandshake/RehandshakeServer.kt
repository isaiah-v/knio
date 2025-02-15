package org.knio.core.test.servers.rehandshake

import org.knio.core.test.servers.TestServer
import org.knio.core.test.servers.read
import org.knio.core.test.servers.readInt
import org.knio.core.test.servers.write
import java.io.IOException
import java.net.SocketException
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLSocket

/**
 * A server to test re-handshaking.
 */
class RehandshakeServer(
    private val serverSocket: SSLServerSocket
): TestServer, Runnable {

    override fun run() {
        while(!serverSocket.isClosed) {
            try {
                runClient(serverSocket.accept() as SSLSocket).start()
            } catch (e: SocketException) {
                // socket closed, probably
            }
        }
    }

    private fun runClient(client: SSLSocket) = Thread {
        client.use {
            val inputStream = client.inputStream
            try {
                val count = client.readInt()!!

                for (i in 0 until count) {
                    if(client.isClosed) {
                        break
                    }

                    if(i>0) {
                        // handshake
                        client.startHandshake()
                    }

                    val str = client.read() ?: return@use
                    client.write(str)

                    if(i<count-1) {
                        client.session.invalidate()
                    }
                }
            } catch (e: IOException) {
                println(e)
            }

            val read = inputStream.read()
            if(read!=-1) {
                throw IOException()
            }
        }
    }.apply {
        isDaemon = true
    }

    override suspend fun start(): TestServer {
        Thread(this).start()
        return this
    }

    override suspend fun stop() {
        serverSocket.close()
    }

    override fun isSSL(): Boolean {
        return true
    }

    override fun getPort(): Int {
        return serverSocket.localPort
    }
}