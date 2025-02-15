package org.knio.core.test.servers.reverse

import kotlinx.coroutines.*
import org.knio.core.io.KInputStream
import org.knio.core.io.KOutputStream
import org.knio.core.lang.use
import org.knio.core.net.KServerSocket
import org.knio.core.net.KSocket
import org.knio.core.net.ssl.KSSLServerSocket
import org.knio.core.net.ssl.KSSLSocket
import org.knio.core.test.servers.TestServer
import org.knio.core.test.servers.read
import org.knio.core.test.servers.write
import java.net.SocketException
import javax.net.ssl.SSLSocket

/**
 * A server that reverses a string written using the classic Knio API.
 *
 * @property serverSocket The server socket to accept client connections.
 */
class KnioReverseServer(
    private val serverSocket: KServerSocket
): TestServer {

    /**
     * Starts the server and listens for client connections.
     * Reverses the input string from the client and sends it back.
     */
    private suspend fun run() = serverSocket.use {
        while (!serverSocket.isClosed()) {
            try {
                runClient(serverSocket.accept())
            } catch (e: SocketException) {
                // socket closed
            }
        }
    }

    private fun runClient(client: KSocket) = CoroutineScope(Dispatchers.Default).launch {
        client.use {
            if(client is SSLSocket) {
                client.startHandshake()
            }

            val inputStream = client.getInputStream()

            // read the input
            val input = client.read() ?: return@use
            val reverse = input.reversed()
            client.write(reverse)

            val read = inputStream.read()
            if(read!=-1) {
                throw Exception("Unexpected data received")
            }
        }
    }

    /**
     * Starts the server
     *
     * @return The instance of KnioReverseServer.
     */
    override suspend fun start(): KnioReverseServer {
        CoroutineScope(Dispatchers.Default).launch {
            run()
        }

        return this
    }

    override suspend fun stop() {
        serverSocket.close()
    }

    override fun isSSL(): Boolean {
        return serverSocket is KSSLServerSocket
    }

    override fun getPort(): Int {
        return runBlocking { serverSocket.getLocalPort() }
    }
}