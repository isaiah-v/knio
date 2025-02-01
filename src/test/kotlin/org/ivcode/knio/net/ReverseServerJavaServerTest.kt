package org.ivcode.knio.net

import org.ivcode.knio.test.servers.createJavaServerSocket
import org.ivcode.knio.test.servers.reverse.JavaReverseServer

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
