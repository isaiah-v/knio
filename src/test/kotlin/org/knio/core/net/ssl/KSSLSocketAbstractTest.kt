package org.knio.core.net.ssl

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.knio.core.lang.use
import org.knio.core.test.servers.TestServerTest
import org.knio.core.test.servers.accept.AcceptOnlyServer
import org.knio.core.test.servers.createJavaServerSocket
import org.knio.core.test.servers.createJavaSocket
import org.knio.core.test.servers.createKnioSocket
import javax.net.ssl.SSLSocket

class KSSLSocketAbstractTest : TestServerTest<AcceptOnlyServer>() {

    @Test
    fun `test supported cypher suites`()  = runServer(true) {

        var supportedCipherSuites: Array<String>

        // java
        createJavaSocket(true).use { client ->
            val sslClient = client as SSLSocket
            val suites = sslClient.supportedCipherSuites
            supportedCipherSuites = suites
            assertTrue(suites.isNotEmpty())
        }

        // knio
        createKnioSocket(true).use { client ->
            val sslClient = client as KSSLSocket
            val suites = sslClient.getSupportedCipherSuites()
            assertTrue(suites.isNotEmpty())

            // make sure java and knio have the same supported cipher suites
            assertTrue(suites.contentEquals(supportedCipherSuites))
        }
    }

    @Test
    fun `test enabled cypher suites`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            val sslClient = client as SSLSocket
            val suites = sslClient.enabledCipherSuites
            assertTrue(suites.isNotEmpty())
        }

        // knio
        createKnioSocket(true).use { client ->
            val sslClient = client as KSSLSocket
            val suites = sslClient.getEnabledCipherSuites()
            assertTrue(suites.isNotEmpty())
        }
    }

    override suspend fun startReverseServer(isSSL: Boolean): AcceptOnlyServer {
        if(!isSSL) {
            throw IllegalArgumentException("SSL is required")
        }

        val server = AcceptOnlyServer(createJavaServerSocket(true))
        server.start()

        return server
    }
}