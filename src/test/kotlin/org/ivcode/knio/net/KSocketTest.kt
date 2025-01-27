package org.ivcode.knio.net

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.lang.use
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.Socket
import java.net.SocketException
import javax.net.ServerSocketFactory
import javax.net.SocketFactory
import kotlin.test.assertEquals

class KSocketTest {

    /**
     * A basic reverse string server using the standard blocking ServerSocket, handling one client at a time.
     */
    private class SimpleReverseServer: AutoCloseable, Runnable {

        private val server = ServerSocketFactory.getDefault().createServerSocket(8080)

        override fun run() {
            try {
                while (!server.isClosed) {
                    server.accept().use { client ->
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
                }
            } catch (e: SocketException) {
                // socket closed
            } finally {
                server.close()
            }
        }


        override fun close() {
            server.close()
        }
    }

    private fun runServer(): AutoCloseable {
        val server = SimpleReverseServer()
        val thread = Thread(server)
        thread.start()

        return server
    }

    @Test
    fun `test basic client`(): Unit = runBlocking {
        val text = "Hello World"
        val expected = text.reversed()


        runServer().use {
            // java
            SocketFactory.getDefault().createSocket("localhost", 8080).use { client ->
                assertEquals(expected, client.reverseString(text))
            }

            // knio
            KSocketFactory.getDefault().createSocket("localhost", 8080).use { client ->
                assertEquals(expected, client.reverseString(text))
            }
        }
    }

    @Test
    fun `test shutdown output stream`():Unit = runBlocking {
        val text = "Hello World"
        val expected = text.reversed()

        runServer().use {
            // java
            SocketFactory.getDefault().createSocket("localhost", 8080).use { client ->
                client.getOutputStream().write(text.toByteArray(Charsets.UTF_8))
                client.shutdownOutput()

                assertThrows<SocketException> {
                    client.getOutputStream()
                }
            }

            // knio
            KSocketFactory.getDefault().createSocket("localhost", 8080).use { client ->
                client.getOutputStream().write(text.toByteArray(Charsets.UTF_8))
                client.shutdownOutput()

                assertThrows<SocketException> {
                    client.getOutputStream()
                }
            }
        }
    }
}

fun Socket.reverseString(input: String): String {
    val str = StringBuilder()

    soTimeout = 4000

    getOutputStream().apply {
        write(input.toByteArray(Charsets.UTF_8))
    }
    shutdownOutput()

    val buffer = ByteArray(1024)
    getInputStream().apply {
        while (true) {
            val read = read(buffer)
            if (read == -1) {
                break
            }

            str.append(String(buffer, 0, read, Charsets.UTF_8))
        }
    }
    shutdownInput()

    return str.toString()
}

suspend fun KSocket.reverseString(input: String): String {
    val str = StringBuilder()

    setReadTimeout(4000)
    setWriteTimeout(4000)

    getOutputStream().apply {
        write(input.toByteArray(Charsets.UTF_8))
    }
    shutdownOutput()

    val buffer = ByteArray(1024)
    getInputStream().apply {
        while (true) {
            val read = read(buffer)
            if (read == -1) {
                break
            }

            str.append(String(buffer, 0, read, Charsets.UTF_8))
        }
    }
    shutdownInput()

    return str.toString()
}
