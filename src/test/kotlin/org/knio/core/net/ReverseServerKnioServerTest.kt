package org.knio.core.net

import org.knio.core.test.servers.createKnioServerSocket
import org.knio.core.test.servers.reverse.KnioReverseServer

/**
 * Runs the ReverseServerTest using KnioReverseServer
 */
class ReverseServerKnioServerTest: ReverseServerTest<KnioReverseServer>() {

    override suspend fun startReverseServer(isSSL: Boolean): KnioReverseServer {
        val server = KnioReverseServer(createKnioServerSocket(isSSL))
        server.start()

        return server
    }

}
