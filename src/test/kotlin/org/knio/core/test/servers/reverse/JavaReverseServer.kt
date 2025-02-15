package org.knio.core.test.servers.reverse

import org.knio.core.test.servers.TestServer
import org.knio.core.test.servers.read
import org.knio.core.test.servers.write
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLSocket

/**
 * A server that reverses a string written using the classic Java API.
 *
 * @property serverSocket The server socket to accept client connections.
 */
class JavaReverseServer(
    private val serverSocket: ServerSocket
): Runnable, TestServer {

    /**
     * Starts the server and listens for client connections.
     * Reverses the input string from the client and sends it back.
     */
    override fun run() = serverSocket.use {
        while (!serverSocket.isClosed) {
            try {
                runClient(serverSocket.accept()).start()
            } catch (e: Throwable) {
                // socket closed
            }
        }
    }

    private fun runClient(client: Socket) = Thread {
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
    }.apply {
        isDaemon = true
    }

    /**
     * Starts the server in a new daemon thread.
     *
     * @return The instance of JavaReverseServer.
     */
    override suspend fun start(): JavaReverseServer {
        val thead = Thread(this)
        thead.isDaemon = true
        thead.start()

        return this
    }

    override suspend fun stop() {
        serverSocket.close()
    }

    override fun isSSL(): Boolean {
        return serverSocket is SSLServerSocket
    }

    override fun getPort(): Int {
        return serverSocket.localPort
    }
}