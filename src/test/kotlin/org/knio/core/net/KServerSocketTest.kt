package org.knio.core.net

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.knio.core.lang.use
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.concurrent.CountDownLatch
import javax.net.ServerSocketFactory
import javax.net.SocketFactory
import kotlin.test.assertTrue
import kotlin.test.fail

class KServerSocketTest {

    @Test
    fun `basic server socket test not bound`(): Unit = runBlocking {
        KServerSocketFactory.getDefault().createServerSocket().use {
            assertFalse(it.isBound())
        }
    }

    @Test
    fun `basic server socket test`(): Unit = runBlocking {
        KServerSocketFactory.getDefault().createServerSocket(8080).use {
            assertTrue(it.isBound())
        }
    }

    @Test
    fun `basic server socket test with backlog`(): Unit = runBlocking {
        KServerSocketFactory.getDefault().createServerSocket(8080, 10).use {
            assertTrue(it.isBound())
        }
    }

    @Test
    fun `basic server socket test with backlog and address`(): Unit = runBlocking {
        KServerSocketFactory.getDefault().createServerSocket(8080, 10, InetAddress.getLoopbackAddress()).use {
            assertTrue(it.isBound())
        }
    }

    @Test
    fun `basic accept test`(): Unit = runBlocking {

        // --== Java ==--
        val javaLatch = CountDownLatch(1)
        var javaAccepted = false

        val serverJob = launch(Dispatchers.IO) {
            ServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
                javaLatch.countDown() // Signal that the server is ready

                // Unit Under Test
                serverSocket.accept().use {
                    javaAccepted = true
                    println("Connection accepted")
                }
            }
        }
        javaLatch.await() // Wait for the server to be ready
        SocketFactory.getDefault().createSocket("localhost", 8080).use {
            // Do nothing
        }

        serverJob.join()
        assertTrue(javaAccepted)

        // --== Knio ==--
        val knioLatch = CountDownLatch(1)
        var knioAccepted = false

        val knioServerJob = launch(Dispatchers.IO) {
            KServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
                knioLatch.countDown() // Signal that the server is ready

                // Unit Under Test
                serverSocket.accept().use {
                    knioAccepted = true
                    println("Connection accepted")
                }
            }
        }
        knioLatch.await() // Wait for the server to be ready
        SocketFactory.getDefault().createSocket("localhost", 8080).use {
            // Do nothing
        }

        knioServerJob.join()
        assertTrue(knioAccepted)
    }

    @Test
    fun `test accept timeout`(): Unit = runBlocking {

        // Java
        try {
            withTimeout(500) {
                ServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
                    serverSocket.soTimeout = 200
                    assertThrows<SocketTimeoutException> {
                        serverSocket.accept()
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            fail("JAVA job timed out")
        }

        // Knio
        try {
            withTimeout(500) {
                KServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
                    serverSocket.setAcceptTimeout(200)
                    assertThrows<SocketTimeoutException> {
                        serverSocket.accept()
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            fail("KNIO job timed out")
        }
    }

    @Test
    fun `test get local address`(): Unit = runBlocking {
        // Java
        ServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            assertTrue(serverSocket.localPort == 8080)
            assertTrue(serverSocket.localSocketAddress != null)
            assertTrue(serverSocket.inetAddress != null)
        }

        // Knio
        KServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            assertTrue(serverSocket.getLocalPort() == 8080)
            assertTrue(serverSocket.getLocalSocketAddress() != null)
            assertTrue(serverSocket.getInetAddress() != null)
        }
    }

    @Test
    fun `test get accept timeout`(): Unit = runBlocking {
        // Java
        ServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            assertTrue(serverSocket.soTimeout == 0)
        }

        // Knio
        KServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            assertTrue(serverSocket.getAcceptTimeout() == 0L)
        }
    }

    @Test
    fun `test set and get accept timeout`(): Unit = runBlocking {
        // Java
        ServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            serverSocket.soTimeout = 100
            assertTrue(serverSocket.soTimeout == 100)
        }

        // Knio
        KServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            serverSocket.setAcceptTimeout(100)
            assertTrue(serverSocket.getAcceptTimeout() == 100L)
        }
    }

    @Test
    fun `test set and get receive buffer size`(): Unit = runBlocking {
        // Java
        ServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            serverSocket.receiveBufferSize = 100
            assertTrue(serverSocket.receiveBufferSize == 100)
        }

        // Knio
        KServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            serverSocket.setReceiveBufferSize(100)
            assertTrue(serverSocket.getReceiveBufferSize() == 100)
        }
    }

    @Test
    fun `test set and get reuse address`(): Unit = runBlocking {
        // Java
        ServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            serverSocket.reuseAddress = true
            assertTrue(serverSocket.reuseAddress)
        }

        // Knio
        KServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            serverSocket.setReuseAddress(true)
            assertTrue(serverSocket.getReuseAddress())
        }
    }

    @Test
    fun `test is closed`() = runBlocking {
        // Java
        ServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            assertFalse(serverSocket.isClosed)
            serverSocket.close()
            assertTrue(serverSocket.isClosed)
        }

        // Knio
        KServerSocketFactory.getDefault().createServerSocket(8080).use { serverSocket ->
            assertFalse(serverSocket.isClosed())
            serverSocket.close()
            assertTrue(serverSocket.isClosed())
        }
    }
}