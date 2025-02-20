package org.knio.core.net.ssl

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.knio.core.lang.use
import java.net.InetAddress
import javax.net.ssl.SSLServerSocketFactory

class KSSLServerSocketFactoryDefaultTest {

    @Test
    fun `basic server socket test not bound`(): Unit = runBlocking {
        // Java
        SSLServerSocketFactory.getDefault().createServerSocket().use {
            assertFalse(it.isBound)
        }

        // Knio
        KSSLServerSocketFactory.getDefault().createServerSocket().use {
            assertFalse(it.isBound())
        }
    }

    @Test
    fun `basic server socket test`(): Unit = runBlocking {
        // Java
        SSLServerSocketFactory.getDefault().createServerSocket(8443).use {
            assertTrue(it.isBound)
        }

        // Knio
        KSSLServerSocketFactory.getDefault().createServerSocket(8443).use {
            assertTrue(it.isBound())
        }
    }

    @Test
    fun `basic server socket test with backlog`(): Unit = runBlocking {
        // Java
        SSLServerSocketFactory.getDefault().createServerSocket(8443, 10).use {
            assertTrue(it.isBound)
        }

        // Knio
        KSSLServerSocketFactory.getDefault().createServerSocket(8443, 10).use {
            assertTrue(it.isBound())
        }
    }

    @Test
    fun `basic server socket test with backlog and address`(): Unit = runBlocking {
        // Java
        SSLServerSocketFactory.getDefault().createServerSocket(8443, 10, InetAddress.getLoopbackAddress()).use {
            assertTrue(it.isBound)
        }

        // Knio
        KSSLServerSocketFactory.getDefault().createServerSocket(8443, 10, InetAddress.getLoopbackAddress()).use {
            assertTrue(it.isBound())
        }
    }
}