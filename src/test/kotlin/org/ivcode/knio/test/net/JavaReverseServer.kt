package org.ivcode.knio.test.net

import java.net.ServerSocket
import java.net.SocketException

/**
 * A server that reverses a string written using the classic Java API
 */
class JavaReverseServer(
    private val serverSocket: ServerSocket
): AutoCloseable, Runnable {
    override fun run() = serverSocket.use {
        while (!serverSocket.isClosed) {
            try {
                serverSocket.accept().use { client ->
                    // read the input
                    val buffer = ByteArray(1024)
                    val builder = StringBuilder()

                    client.getInputStream().apply {
                        while (true) {
                            val read = read(buffer)
                            if (read == -1) {
                                break
                            }

                            builder.append(String(buffer, 0, read, Charsets.UTF_8))
                        }
                    }
                    client.shutdownInput()


                    val reverse = builder.toString().reversed()

                    // write the output
                    client.getOutputStream().apply {
                        write(reverse.toByteArray(Charsets.UTF_8))
                    }
                    client.shutdownOutput()
                }
            } catch (e: SocketException) {
                // socket closed
            }
        }

    }

    override fun close() {
        serverSocket.close()
    }

    fun start(): JavaReverseServer {
        val thead = Thread(this)
        thead.isDaemon = true
        thead.start()

        return this
    }
}
