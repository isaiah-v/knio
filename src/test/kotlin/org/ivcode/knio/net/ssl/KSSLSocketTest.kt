package org.ivcode.knio.net.ssl

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.lang.use
import org.ivcode.knio.net.reverseString
import org.ivcode.knio.test.utils.createTestSSLContext
import org.ivcode.knio.test.utils.createTrustAllSSLContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.SocketException
import kotlin.test.assertEquals

class KSSLSocketTest {
    /**
     * A basic reverse string server using the standard blocking ServerSocket, handling one client at a time.
     */
    private class SSLSimpleReverseServer: AutoCloseable, Runnable {

        private val server = createTestSSLContext().serverSocketFactory.createServerSocket(8443)

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
        val server = SSLSimpleReverseServer()
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
            createTrustAllSSLContext().socketFactory.createSocket("localhost", 8443).use { client ->
                assertEquals(expected, client.reverseString(text))
            }


            // knio
            createTrustAllSSLContext().getKnioSSLSocketFactory().createSocket("localhost", 8443).use { client ->
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
            createTrustAllSSLContext().socketFactory.createSocket("localhost", 8443).use { client ->
                client.getOutputStream().write(text.toByteArray(Charsets.UTF_8))
                client.shutdownOutput()

                assertThrows<SocketException> {
                    client.getOutputStream()
                }
            }

            // knio
            createTrustAllSSLContext().getKnioSSLSocketFactory().createSocket("localhost", 8443).use { client ->
                client.getOutputStream().write(text.toByteArray(Charsets.UTF_8))
                client.shutdownOutput()

                assertThrows<SocketException> {
                    client.getOutputStream()
                }
            }
        }
    }

}