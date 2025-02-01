package org.ivcode.knio.net

import org.ivcode.knio.lang.use
import org.ivcode.knio.test.servers.TestServer
import org.ivcode.knio.test.servers.TestServerTest
import org.ivcode.knio.test.servers.createJavaSocket
import org.ivcode.knio.test.servers.createKnioSocket
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Abstract test class for testing the ReverseServer.
 */
abstract class ReverseServerTest<T: TestServer>: TestServerTest<T>() {

    /**
     * Tests the basic client functionality with and without SSL.
     *
     * @param isSSL Whether to use SSL.
     */
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test basic client`(isSSL: Boolean) = runServer(isSSL) {
        val text = "Hello World"
        val expected = text.reversed()

        // java
        createJavaSocket(isSSL).use { client ->
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

    /**
     * Tests the behavior when the output stream is shut down.
     *
     * @param isSSL Whether to use SSL.
     */
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test shutdown output stream`(isSSL: Boolean) = runServer(isSSL){
        val text = "Hello World"

        // java
        createJavaSocket(isSSL).use { client ->
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

    /**
     * Tests that closing the output stream closes the connection.
     *
     * @param isSSL Whether to use SSL.
     */
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test closing output-stream closes connection`(isSSL: Boolean) = runServer(isSSL) {
        val text = "Hello World"

        // java
        createJavaSocket(isSSL).use { client ->
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

    /**
     * Tests that closing the input stream closes the connection.
     *
     * @param isSSL Whether to use SSL.
     */
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test closing input-stream closes connection`(isSSL: Boolean) = runServer(isSSL) {
        val text = "Hello World"
        val expected = text.reversed()

        // java
        createJavaSocket(isSSL).use { client ->
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

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test read timeout`(isSSL: Boolean) = runServer(isSSL) {
        // java
        createJavaSocket(isSSL).use { client ->
            client.soTimeout = 1000

            assertThrows<SocketTimeoutException> {
                client.getInputStream().read()
            }
        }

        // knio
        createKnioSocket(isSSL).use { client ->
            client.setReadTimeout(1000)

            assertThrows<SocketTimeoutException> {
                client.getInputStream().read()
            }
        }
    }

}