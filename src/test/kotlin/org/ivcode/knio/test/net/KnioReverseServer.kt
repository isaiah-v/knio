package org.ivcode.knio.test.net

import kotlinx.coroutines.*
import org.ivcode.knio.io.KInputStream
import org.ivcode.knio.io.KOutputStream
import org.ivcode.knio.lang.KAutoCloseable
import org.ivcode.knio.lang.use
import org.ivcode.knio.net.KServerSocket
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.SocketException

/**
 * A server that reverses a string written using the classic Java API.
 *
 * @property serverSocket The server socket to accept client connections.
 */
class KnioReverseServer(
    private val serverSocket: KServerSocket
): KAutoCloseable {

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
     * Closes the server socket.
     */
    override suspend fun close() {
        serverSocket.close()
    }

    /**
     * Starts the server in a new daemon thread.
     *
     * @return The instance of JavaReverseServer.
     */
    suspend fun start(): KnioReverseServer {
        CoroutineScope(Dispatchers.IO).launch {
            run()
        }

        return this
    }
}