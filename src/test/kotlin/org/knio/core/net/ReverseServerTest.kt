package org.knio.core.net

import org.knio.core.lang.use
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.knio.core.test.servers.*
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
        createJavaSocket().use { client ->
            client.soTimeout = 1000

            client.write(text)
            val data = client.read()

            assertEquals(expected, data)
        }

        // knio
        createKnioSocket().use { client ->
            client.setReadTimeout(1000)
            client.setWriteTimeout(1000)

            client.write(text)
            val data = client.read()

            assertEquals(expected, data)
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
        createJavaSocket().use { client ->
            client.write(text)
            client.shutdownOutput()

            assertThrows<SocketException> {
                client.getOutputStream()
            }
        }

        // knio
        createKnioSocket().use { client ->
            client.write(text)
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
        createJavaSocket().use { client ->
            client.soTimeout = 1000

            client.getOutputStream().use { output ->
                client.write(text)

                // open before close
                assertFalse(client.isClosed)
            } // <- output-stream closed

            // the socket closes when the output stream is closed
            assertTrue(client.isClosed)
        }

        // knio
        createKnioSocket().use { client ->
            client.setReadTimeout(1000)
            client.setWriteTimeout(1000)

            client.getOutputStream().use { output ->
                client.write(text)

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

        // java
        createJavaSocket().use { client ->
            client.soTimeout = 1000

            client.write(text)
            client.getInputStream().use { input ->
                client.read()

                // before closing
                assertFalse(client.isClosed)
            } // <- input-stream closed

            // after close
            assertTrue(client.isClosed)
        }

        // knio
        createKnioSocket().use { client ->
            val str = StringBuilder()

            client.setWriteTimeout(1000)
            client.setReadTimeout(1000)

            client.write(text)
            client.getInputStream().use { input ->
                client.read()

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
        createJavaSocket().use { client ->
            client.soTimeout = 1000

            assertThrows<SocketTimeoutException> {
                client.getInputStream().read()
            }
        }

        // knio
        createKnioSocket().use { client ->
            client.setReadTimeout(1000)

            assertThrows<SocketTimeoutException> {
                client.getInputStream().read()
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true])
    fun `test writing to shutdown output`(isSSL: Boolean) = runServer(isSSL) {

        // Documentations states an IOException should be thrown
        // The IOException thrown is a SocketException

        // java
        createJavaSocket().use { client ->
            client.shutdownOutput()

            assertThrows<SocketException> {
                client.getOutputStream().write(0)
            }
        }

        // knio
        createKnioSocket().use { client ->
            client.shutdownOutput()

            assertThrows<SocketException> {
                client.getOutputStream().write(0)
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test getting input stream after shutdown input`(isSSL: Boolean) = runServer(isSSL) {
        // java
        createJavaSocket().use { client ->
            client.shutdownInput()

            assertThrows<SocketException> {
                client.getInputStream()
            }
        }

        // knio
        createKnioSocket().use { client ->
            client.shutdownInput()

            assertThrows<SocketException> {
                client.getInputStream()
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test reading from shutdown input`(isSSL: Boolean) = runServer(isSSL) {

        // Documentations states an IOException should be thrown
        // The IOException thrown is a SocketException

        // java
        createJavaSocket().use { client ->
            val inputStream = client.getInputStream()
            client.shutdownInput()

            assertEquals(-1, inputStream.read())
        }

        // knio
        createKnioSocket().use { client ->
            val inputStream = client.getInputStream()
            client.shutdownInput()

            assertEquals(-1, inputStream.read())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test getting input stream after close`(isSSL: Boolean) = runServer(isSSL) {
        // java
        createJavaSocket().use { client ->
            client.close()

            assertThrows<SocketException> {
                client.getInputStream()
            }
        }

        // knio
        createKnioSocket().use { client ->
            client.close()

            assertThrows<SocketException> {
                client.getInputStream()
            }
        }
    }
}