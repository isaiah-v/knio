package org.ivcode.knio.net

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.lang.use
import org.ivcode.knio.net.ssl.getKnioSSLSocketFactory
import org.ivcode.knio.test.servers.reverse.ReverseServer
import org.ivcode.knio.test.utils.createTrustAllSSLContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.Socket
import java.net.SocketException
import javax.net.SocketFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class ReverseServerTest {

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test basic client`(isSSL: Boolean) = runServer(isSSL) {
        val text = "Hello World"
        val expected = text.reversed()

        // java
        createSocket(isSSL).use { client ->
            val str = StringBuilder()

            client.soTimeout = 4000

            client.getOutputStream().apply {
                write(text.toByteArray(Charsets.UTF_8))
            }
            client.shutdownOutput()

            client.getInputStream().apply {
                val data = readAllBytes()
                str.append(String(data, Charsets.UTF_8))
            }
            client.shutdownInput()

            assertEquals(expected, str.toString())
        }

        // knio
        createKnioSocket(isSSL).use { client ->
            val str = StringBuilder()

            client.setReadTimeout(4000)
            client.setWriteTimeout(4000)

            client.getOutputStream().apply {
                write(text.toByteArray(Charsets.UTF_8))
            }
            client.shutdownOutput()

            client.getInputStream().apply {
                val data = readAllBytes()
                str.append(String(data, Charsets.UTF_8))
            }
            client.shutdownInput()

            assertEquals(expected, str.toString())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test shutdown output stream`(isSSL: Boolean) = runServer(isSSL) {
        val text = "Hello World"

        // java
        createSocket(isSSL).use { client ->
            client.getOutputStream().write(text.toByteArray(Charsets.UTF_8))
            client.shutdownOutput()

            assertThrows<SocketException> {
                client.getOutputStream()
            }
        }

        // knio
        createKnioSocket(isSSL).use { client ->
            client.getOutputStream().write(text.toByteArray(Charsets.UTF_8))
            client.shutdownOutput()

            assertThrows<SocketException> {
                client.getOutputStream()
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test closing output-stream closes connection`(isSSL: Boolean) = runServer(isSSL) {
        val text = "Hello World"
        val expected = text.reversed()

        // java
        createSocket(isSSL).use { client ->
            client.soTimeout = 4000

            client.getOutputStream().use { output ->
                output.write(text.toByteArray(Charsets.UTF_8))

                // open before close
                assertFalse(client.isClosed)
            } // <- output-stream closed

            // the socket closes when the output stream is closed
            assertTrue(client.isClosed)
        }

        // knio
        createKnioSocket(isSSL).use { client ->
            client.setReadTimeout(4000)
            client.setWriteTimeout(4000)

            client.getOutputStream().use { output ->
                output.write(text.toByteArray(Charsets.UTF_8))

                // open before close
                assertFalse(client.isClosed())
            } // <- output-stream closed

            // the socket closes when the output stream is closed
            assertTrue(client.isClosed())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test closing input-stream closes connection`(isSSL: Boolean) = runServer(isSSL) {
        val text = "Hello World"
        val expected = text.reversed()

        // java
        createSocket(isSSL).use { client ->
            val str = StringBuilder()

            client.soTimeout = 4000

            client.getOutputStream().apply {
                write(text.toByteArray(Charsets.UTF_8))
            }
            client.shutdownOutput()

            client.getInputStream().use { input ->
                val data = input.readAllBytes()
                str.append(String(data, Charsets.UTF_8))

                // before closing
                assertFalse(client.isClosed)
            } // <- input-stream closed

            // after close
            assertTrue(client.isClosed)
        }

        // knio
        createKnioSocket(isSSL).use { client ->
            val str = StringBuilder()

            client.setWriteTimeout(4000)
            client.setReadTimeout(4000)

            client.getOutputStream().apply {
                write(text.toByteArray(Charsets.UTF_8))
            }
            client.shutdownOutput()

            client.getInputStream().use { input ->
                val data = input.readAllBytes()
                str.append(String(data, Charsets.UTF_8))

                // before closing
                assertFalse(client.isClosed())
            } // <- input-stream closed

            // after close
            assertTrue(client.isClosed())
        }
    }

    private fun createSocket(isSSL: Boolean): Socket = if(isSSL) {
        createTrustAllSSLContext().socketFactory.createSocket("localhost", 8443)
    } else {
        SocketFactory.getDefault().createSocket("localhost", 8080)
    }

    private suspend fun createKnioSocket(isSSL: Boolean): KSocket = if (isSSL) {
        createTrustAllSSLContext().getKnioSSLSocketFactory().createSocket("localhost", 8443)
    } else {
        KSocketFactory.getDefault().createSocket("localhost", 8080)
    }

    protected abstract suspend fun startReverseServer(isSSL: Boolean): ReverseServer

    private fun runServer(isSSL: Boolean, block: suspend () -> Unit) = runBlocking {
        val server = startReverseServer(isSSL)
        try {
            block()
        } finally {
            server.stop()
        }
    }

}
