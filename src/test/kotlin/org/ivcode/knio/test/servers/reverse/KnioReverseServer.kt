package org.ivcode.knio.test.servers.reverse

import kotlinx.coroutines.*
import org.ivcode.knio.io.KInputStream
import org.ivcode.knio.io.KOutputStream
import org.ivcode.knio.lang.KAutoCloseable
import org.ivcode.knio.lang.use
import org.ivcode.knio.net.KServerSocket
import java.net.SocketException

/**
 * A server that reverses a string written using the classic Knio API.
 *
 * @property serverSocket The server socket to accept client connections.
 */
class KnioReverseServer(
    private val serverSocket: KServerSocket
): ReverseServer {

    /**
     * Starts the server and listens for client connections.
     * Reverses the input string from the client and sends it back.
     */
    suspend fun run() = serverSocket.use {
        while (!serverSocket.isClosed()) {
            try {
                serverSocket.accept().use { client ->
                    // read the input
                    val input = readInput(client.getInputStream())
                    client.shutdownInput()

                    // reverse the input
                    val reverse = input.reversed()

                    // write the reversed input
                    writeOutput(reverse, client.getOutputStream())
                }
            } catch (e: SocketException) {
                // socket closed
            }
        }
    }

    /**
     * Reads the input from the given InputStream.
     *
     * @param input The InputStream to read from.
     * @return The string read from the input.
     */
    private suspend fun readInput(input: KInputStream): String {
        val buffer = ByteArray(1024)
        val builder = StringBuilder()

        while (true) {
            val read = input.read(buffer)
            if (read == -1) {
                break
            }

            builder.append(String(buffer, 0, read, Charsets.UTF_8))
        }

        return builder.toString()
    }

    /**
     * Writes the given string to the given OutputStream.
     *
     * @param str The string to write.
     * @param output The OutputStream to write to.
     */
    private suspend fun writeOutput(str: String, output: KOutputStream) {
        output.write(str.toByteArray(Charsets.UTF_8))
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
}