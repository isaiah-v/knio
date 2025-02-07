package org.knio.core.net

import org.knio.core.test.servers.createJavaServerSocket
import org.knio.core.test.servers.reverse.JavaReverseServer

/**
 * Runs the ReverseServerTest using JavaReverseServer
 */
class ReverseServerJavaServerTest: ReverseServerTest<JavaReverseServer>() {

    override suspend fun startReverseServer(isSSL: Boolean): JavaReverseServer {
        val server = JavaReverseServer(createJavaServerSocket(isSSL))
        server.start()

        return server
    }
}
