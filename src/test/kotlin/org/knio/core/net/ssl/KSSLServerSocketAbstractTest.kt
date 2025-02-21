package org.knio.core.net.ssl

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.knio.core.lang.use
import java.net.InetAddress
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLServerSocketFactory

class KSSLServerSocketAbstractTest {

    @Test
    fun `test get enabled cipher suites`(): Unit = runBlocking {
        val expected = SSLContext.getDefault().defaultSSLParameters.cipherSuites

        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            assertArrayEquals(expected, socket.enabledCipherSuites)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertArrayEquals(expected, socket.getEnabledCipherSuites())
        }
    }

    @Test
    fun `test get enabled protocols`(): Unit = runBlocking {
        val expected = SSLContext.getDefault().defaultSSLParameters.protocols

        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            assertArrayEquals(expected, socket.enabledProtocols)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertArrayEquals(expected, socket.getEnabledProtocols())
        }
    }

    @Test
    fun `test get enable session creation`(): Unit = runBlocking {
        val expected = true

        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            assertEquals(expected, socket.enableSessionCreation)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertEquals(expected, socket.getEnableSessionCreation())
        }
    }

    @Test
    fun `test get need client auth`(): Unit = runBlocking {
        val expected = SSLContext.getDefault().defaultSSLParameters.needClientAuth

        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            assertEquals(expected, socket.needClientAuth)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertEquals(expected, socket.getNeedClientAuth())
        }
    }

    @Test
    fun `test get ssl parameters`(): Unit = runBlocking {
        val expected = SSLContext.getDefault().defaultSSLParameters

        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            socket.sslParameters.apply {
                assertArrayEquals(expected.cipherSuites, cipherSuites)
                assertArrayEquals(expected.protocols, protocols)
                assertEquals(expected.needClientAuth, needClientAuth)
                assertEquals(expected.serverNames, serverNames)
                assertEquals(expected.wantClientAuth, wantClientAuth)
                assertEquals(expected.sniMatchers, sniMatchers)
            }
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket.getSSLParameters().apply {
                assertArrayEquals(expected.cipherSuites, cipherSuites)
                assertArrayEquals(expected.protocols, protocols)
                assertEquals(expected.needClientAuth, needClientAuth)
                assertEquals(expected.serverNames, serverNames)
                assertEquals(expected.wantClientAuth, wantClientAuth)
                assertEquals(expected.sniMatchers, sniMatchers)
            }
        }
    }

    @Test
    fun `test get supported cipher suites`(): Unit = runBlocking {
        val expected = SSLContext.getDefault().supportedSSLParameters.cipherSuites

        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            assertArrayEquals(expected, socket.supportedCipherSuites)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertArrayEquals(expected, socket.getSupportedCipherSuites())
        }
    }

    @Test
    fun `test get supported protocols`(): Unit = runBlocking {
        val expected = SSLContext.getDefault().supportedSSLParameters.protocols

        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            assertArrayEquals(expected, socket.supportedProtocols)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertArrayEquals(expected, socket.getSupportedProtocols())
        }
    }

    @Test
    fun `test get use client mode`(): Unit = runBlocking {
        val expected = false

        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            assertEquals(expected, socket.useClientMode)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertEquals(expected, socket.getUseClientMode())
        }
    }

    @Test
    fun `test get want client auth`(): Unit = runBlocking {
        val expected = SSLContext.getDefault().defaultSSLParameters.wantClientAuth

        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            assertEquals(expected, socket.wantClientAuth)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertEquals(expected, socket.getWantClientAuth())
        }
    }

    @Test
    fun `test get InetAddress, no connect`(): Unit = runBlocking {
        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            assertNull(socket.inetAddress)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertNull(socket.getInetAddress())
        }
    }

    @Test
    fun `test get InetAddress, connected`(): Unit = runBlocking {
        // java
        SSLServerSocketFactory.getDefault().createServerSocket(8443).use { socket ->
            socket as SSLServerSocket
            assertTrue(socket.inetAddress.isAnyLocalAddress)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket(8443).use { socket ->
            assertNotNull(socket.getInetAddress()!!.isAnyLocalAddress)
        }
    }

    @Test
    fun `test get InetAddress, connected with address`(): Unit = runBlocking {
        // java
        SSLServerSocketFactory.getDefault().createServerSocket(8443, 10, InetAddress.getLoopbackAddress()).use { socket ->
            socket as SSLServerSocket
            assertEquals(InetAddress.getLoopbackAddress(), socket.inetAddress)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket(8443, 10, InetAddress.getLoopbackAddress()).use { socket ->
            assertEquals(InetAddress.getLoopbackAddress(), socket.getInetAddress())
        }
    }

    @Test
    fun `test get InetAddress, connected with address and backlog`(): Unit = runBlocking {
        // java
        SSLServerSocketFactory.getDefault().createServerSocket(8443, 10, InetAddress.getLoopbackAddress()).use { socket ->
            socket as SSLServerSocket
            assertEquals(InetAddress.getLoopbackAddress(), socket.inetAddress)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket(8443, 10, InetAddress.getLoopbackAddress()).use { socket ->
            assertEquals(InetAddress.getLoopbackAddress(), socket.getInetAddress())
        }
    }

    @Test
    fun `test get local port, no connect`(): Unit = runBlocking {
        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            socket as SSLServerSocket
            assertEquals(-1, socket.localPort)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertEquals(-1, socket.getLocalPort())
        }
    }

    @Test
    fun `test get local port, connected`(): Unit = runBlocking {
        // java
        SSLServerSocketFactory.getDefault().createServerSocket(8443).use { socket ->
            socket as SSLServerSocket
            assertTrue(socket.localPort > 0)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket(8443).use { socket ->
            assertTrue(socket.getLocalPort() > 0)
        }
    }

    @Test
    fun `test get local socket address, no connect`(): Unit = runBlocking {
        // java
        SSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertNull(socket.localSocketAddress)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use { socket ->
            assertNull(socket.getLocalSocketAddress())
        }
    }

    @Test
    fun `test get local socket address, connected`(): Unit = runBlocking {
        // java
        SSLServerSocketFactory.getDefault().createServerSocket(8443).use { socket ->
            assertNotNull(socket.localSocketAddress)
        }

        // knio
        KSSLServerSocketFactory.getDefault().createServerSocket(8443).use { socket ->
            assertNotNull(socket.getLocalSocketAddress())
        }
    }
}