package org.ivcode.knio.net

import org.ivcode.knio.net.ssl.getKnioSSLServerSocketFactory
import org.ivcode.knio.test.servers.reverse.KnioReverseServer
import org.ivcode.knio.test.servers.reverse.ReverseServer
import org.ivcode.knio.test.utils.createTestSSLContext

/**
 * Runs the ReverseServerTest using KnioReverseServer
 */
class ReverseServerKnioTest: ReverseServerTest() {
    override suspend fun startReverseServer(isSSL: Boolean): ReverseServer {
        val serverSocket = if(isSSL) {
            createTestSSLContext().getKnioSSLServerSocketFactory().createServerSocket(8443)
        } else {
            KServerSocketFactory.getDefault().createServerSocket(8080)
        }
        val server = KnioReverseServer(serverSocket)
        server.start()

        return server
    }
}
