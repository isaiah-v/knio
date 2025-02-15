package org.knio.core.net.ssl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.knio.core.lang.use
import org.knio.core.test.servers.*
import org.knio.core.test.servers.rehandshake.RehandshakeServer
import java.security.Security


private const val PROTOCOL = "TLSv1.2"

/**
 * Tests for re-handshaking scenarios.
 *
 * @see RehandshakeServer
 */
class RehandshakeTest: TestServerTest<RehandshakeServer>() {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setSecurity(): Unit {
            Security.setProperty("jdk.tls.disabledAlgorithms", "");
        }
    }

    private var protocol = PROTOCOL

    @Test
    fun `test single handshake`() = runServer(true) {
        // Java
        createJavaSSLSocket(protocol).use { sslClient ->
            val expected = "hello world"

            sslClient.write(1)
            sslClient.write(expected)
            val result = sslClient.read()

            assertEquals(expected, result)
        }

        // Knio
        createKnioSSLSocket(protocol).use { sslClient ->
            val expected = "hello world"

            sslClient.write(1)
            sslClient.write(expected)
            val result = sslClient.read()

            assertEquals(expected, result)
        }
    }

    @ParameterizedTest
    //@ValueSource(strings = ["TLSv1.3", "TLSv1.2", "TLSv1.1", ""SSLv3""])
    @ValueSource(strings = ["TLSv1.3", "TLSv1.2"])
    fun `test multiple handshakes`(protocol: String) {
        this.protocol = protocol

        runServer(true) {
            // on each iteration, the server will request a re-handshake

            // Java
            createJavaSSLSocket(protocol).use { sslClient ->
                val expectedList = listOf(
                    "test 1",
                    "test 2",
                    "test 3",
                    "test 4",
                    "test 5",
                )

                sslClient.write(expectedList.size)
                for (expected in expectedList) {
                    sslClient.write(expected)
                    val result = sslClient.read()

                    assertEquals(expected, result, "java failed with $protocol")
                }
            }


            // Knio
            createKnioSSLSocket(protocol).use { sslClient ->
                val expectedList = listOf(
                    "test 1",
                    "test 2",
                    "test 3",
                    "test 4",
                    "test 5",
                )

                sslClient.write(expectedList.size)
                for (expected in expectedList) {
                    sslClient.write(expected)
                    val result = sslClient.read()

                    assertEquals(expected, result, "knio failed with $protocol")
                }
            }
        }
    }


    override suspend fun startReverseServer(isSSL: Boolean): RehandshakeServer {
        if(!isSSL) {
            throw IllegalArgumentException("SSL is required")
        }

        val server = RehandshakeServer(createJavaSSLServerSocket(PROTOCOL))
        server.start()

        return server
    }
}