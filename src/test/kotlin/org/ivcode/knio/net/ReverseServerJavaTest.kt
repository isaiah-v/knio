package org.ivcode.knio.net

import org.ivcode.knio.test.servers.reverse.JavaReverseServer
import org.ivcode.knio.test.servers.reverse.ReverseServer
import org.ivcode.knio.test.utils.createTestSSLContext
import javax.net.ServerSocketFactory

/**
 * Runs the ReverseServerTest using JavaReverseServer
 */
class ReverseServerJavaTest: ReverseServerTest() {
    override suspend fun startReverseServer(isSSL: Boolean): ReverseServer {
        val serverSocket = if(isSSL) {
            createTestSSLContext().serverSocketFactory.createServerSocket(8443)
        } else {
            ServerSocketFactory.getDefault().createServerSocket(8080)
        }
        val server = JavaReverseServer(serverSocket)
        server.start()

        return server
    }
}
