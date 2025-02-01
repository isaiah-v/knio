package org.ivcode.knio.test.servers

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.net.KServerSocket
import org.ivcode.knio.net.KServerSocketFactory
import org.ivcode.knio.net.KSocket
import org.ivcode.knio.net.KSocketFactory
import org.ivcode.knio.net.ssl.getKnioSSLServerSocketFactory
import org.ivcode.knio.net.ssl.getKnioSSLSocketFactory
import org.ivcode.knio.test.utils.createTestSSLContext
import org.ivcode.knio.test.utils.createTrustAllSSLContext
import java.net.ServerSocket
import java.net.Socket
import javax.net.ServerSocketFactory
import javax.net.SocketFactory

const val PORT = 8080
const val SSL_PORT = 8443

abstract class TestServerTest<T: TestServer> {

    /**
     * Starts the reverse server.
     *
     * @param isSSL Whether to use SSL.
     * @return The started reverse server.
     */
    protected abstract suspend fun startReverseServer(isSSL: Boolean): T


    /**
     * Runs the test server, starting and stopping it before and after the block.
     *
     * @param isSSL Whether to use SSL.
     * @param block The block to run.
     */
    protected fun runServer(isSSL: Boolean, block: suspend T.() -> Unit) = runBlocking {
        val server = startReverseServer(isSSL)
        try {
            server.block()
        } finally {
            server.stop()
        }
    }
}

fun createJavaServerSocket(isSSL: Boolean): ServerSocket = if(isSSL) {
    createTestSSLContext().serverSocketFactory.createServerSocket(SSL_PORT)
} else {
    ServerSocketFactory.getDefault().createServerSocket(PORT)
}

fun createJavaSocket(isSSL: Boolean): Socket = if(isSSL) {
    createTrustAllSSLContext().socketFactory.createSocket("localhost", SSL_PORT)
} else {
    SocketFactory.getDefault().createSocket("localhost", PORT)
}

suspend fun createKnioServerSocket(isSSL: Boolean): KServerSocket = if(isSSL) {
    createTestSSLContext().getKnioSSLServerSocketFactory().createServerSocket(SSL_PORT)
} else {
    KServerSocketFactory.getDefault().createServerSocket(PORT)
}

suspend fun createKnioSocket(isSSL: Boolean): KSocket = if (isSSL) {
    createTrustAllSSLContext().getKnioSSLSocketFactory().createSocket("localhost", SSL_PORT)
} else {
    KSocketFactory.getDefault().createSocket("localhost", PORT)
}
