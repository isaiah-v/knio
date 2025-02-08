package org.knio.core.net.ssl

import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.knio.core.lang.use
import org.knio.core.test.servers.TestServerTest
import org.knio.core.test.servers.accept.AcceptOnlyServer
import org.knio.core.test.servers.createJavaServerSocket
import org.knio.core.test.servers.createJavaSocket
import org.knio.core.test.servers.createKnioSocket
import javax.net.ssl.HandshakeCompletedListener
import javax.net.ssl.SSLSocket
import kotlin.test.assertEquals

class KSSLSocketAbstractTest : TestServerTest<AcceptOnlyServer>() {

    @Test
    fun `test get supported cypher suites`()  = runServer(true) {

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
    fun `test get enabled cypher suites`()  = runServer(true) {
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

    @Test
    fun `test get supported protocols`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            val sslClient = client as SSLSocket
            val protocols = sslClient.supportedProtocols
            assertTrue(protocols.isNotEmpty())
        }

        // knio
        createKnioSocket(true).use { client ->
            val sslClient = client as KSSLSocket
            val protocols = sslClient.getSupportedProtocols()
            assertTrue(protocols.isNotEmpty())
        }
    }

    @Test
    fun `test get enabled protocols`()  = runServer(true) {
        val enabledProtocols: Array<String>

        // java
        createJavaSocket(true).use { client ->
            val sslClient = client as SSLSocket
            val protocols = sslClient.enabledProtocols
            assertTrue(protocols.isNotEmpty())
            enabledProtocols = protocols
        }

        // knio
        createKnioSocket(true).use { client ->
            val sslClient = client as KSSLSocket
            val protocols = sslClient.getEnabledProtocols()
            assertTrue(protocols.isNotEmpty())
            assertTrue(protocols.contentEquals(enabledProtocols))
        }
    }

    @Test
    fun `test get session`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            val sslClient = client as SSLSocket
            val session = sslClient.session
            assertTrue(session.isValid)
        }

        // knio
        createKnioSocket(true).use { client ->
            val sslClient = client as KSSLSocket
            val session = sslClient.getSession()
            assertTrue(session.isValid)
        }
    }

    @Test
    fun `test get handshake session before handshake`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            val sslClient = client as SSLSocket
            val session = sslClient.handshakeSession
            assertEquals(null, session)
        }

        // knio
        createKnioSocket(true).use { client ->
            val sslClient = client as KSSLSocket
            val session = sslClient.getHandshakeSession()
            assertEquals(null, session)
        }
    }

    @Test
    fun `test get handshake session after handshake`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            val sslClient = client as SSLSocket
            sslClient.startHandshake()
            val session = sslClient.handshakeSession
            assertEquals(null, session)
        }

        // knio
        createKnioSocket(true).use { client ->
            val sslClient = client as KSSLSocket
            sslClient.startHandshake()
            val session = sslClient.getHandshakeSession()
            assertEquals(null, session)
        }
    }

    //@Test
    fun `test get handshake sessions during handshake`()  = runServer(true) {
        // TODO how?
    }

    @Test
    fun `test add handshake completed listener`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            var isCalled = false

            val sslClient = client as SSLSocket
            val listener = HandshakeCompletedListener {
                isCalled = true
            }
            sslClient.addHandshakeCompletedListener(listener)

            sslClient.startHandshake()
            delay(100)
            assertTrue(isCalled)
        }

        // knio
        createKnioSocket(true).use { client ->
            var isCalled = false

            val sslClient = client as KSSLSocket
            val listener = KHandshakeCompletedListener {
                isCalled = true
            }
            sslClient.addHandshakeCompletedListener(listener)

            sslClient.startHandshake()
            delay(100)
            assertTrue(isCalled)
        }
    }

    @Test
    fun `test add handshake completed listener after handshake`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            var isCalled = false

            val sslClient = client as SSLSocket
            val listener = HandshakeCompletedListener {
                isCalled = true
            }

            sslClient.startHandshake()

            sslClient.addHandshakeCompletedListener(listener)
            delay(100)
            assertFalse(isCalled)
        }

        // knio
        createKnioSocket(true).use { client ->
            var isCalled = false

            val sslClient = client as KSSLSocket
            val listener = KHandshakeCompletedListener {
                isCalled = true
            }

            sslClient.startHandshake()

            sslClient.addHandshakeCompletedListener(listener)
            delay(100)
            assertFalse(isCalled)
        }
    }

    @Test
    fun `test add handshake completed listener after handshake but called again`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            var isCalled = false

            val sslClient = client as SSLSocket
            val listener = HandshakeCompletedListener {
                isCalled = true
            }

            sslClient.startHandshake()
            sslClient.addHandshakeCompletedListener(listener)
            sslClient.startHandshake()

            delay(100)
            assertFalse(isCalled)
        }

        // knio
        createKnioSocket(true).use { client ->
            var isCalled = false

            val sslClient = client as KSSLSocket
            val listener = KHandshakeCompletedListener {
                isCalled = true
            }

            sslClient.startHandshake()
            sslClient.addHandshakeCompletedListener(listener)
            sslClient.startHandshake()

            delay(100)
            assertFalse(isCalled)
        }
    }

    @Test
    fun `test remove handshake completed listener`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            var isCalled = false

            val sslClient = client as SSLSocket
            val listener = HandshakeCompletedListener {
                isCalled = true
            }
            sslClient.addHandshakeCompletedListener(listener)
            sslClient.removeHandshakeCompletedListener(listener)

            sslClient.startHandshake()
            delay(100)
            assertFalse(isCalled)
        }

        // knio
        createKnioSocket(true).use { client ->
            var isCalled = false

            val sslClient = client as KSSLSocket
            val listener = KHandshakeCompletedListener {
                isCalled = true
            }
            sslClient.addHandshakeCompletedListener(listener)
            sslClient.removeHandshakeCompletedListener(listener)

            sslClient.startHandshake()
            delay(100)
            assertFalse(isCalled)
        }
    }

    @Test
    fun `test get use client mode`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            val sslClient = client as SSLSocket
            assertTrue(sslClient.useClientMode)
        }

        // knio
        createKnioSocket(true).use { client ->
            val sslClient = client as KSSLSocket
            assertTrue(sslClient.getUseClientMode())
        }
    }

    @Test
    fun `test set use client mode`()  = runServer(true) {
        // java
        createJavaSocket(true).use { client ->
            val sslClient = client as SSLSocket
            sslClient.useClientMode = false
            assertFalse(sslClient.useClientMode)
        }

        // knio
        createKnioSocket(true).use { client ->
            val sslClient = client as KSSLSocket
            sslClient.setUseClientMode(false)
            assertFalse(sslClient.getUseClientMode())
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