package org.ivcode.knio.net

import org.ivcode.knio.lang.use
import org.ivcode.knio.test.servers.TestServerTest
import org.ivcode.knio.test.servers.accept.AcceptOnlyServer
import org.ivcode.knio.test.servers.createJavaServerSocket
import org.ivcode.knio.test.servers.createJavaSocket
import org.ivcode.knio.test.servers.createKnioSocket
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.SocketTimeoutException

class ReadWriteTimoutTest: TestServerTest<AcceptOnlyServer>() {

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test read timeout`(isSSL: Boolean) = runServer(isSSL) {
        // java
        createJavaSocket(isSSL).use { client ->
            client.soTimeout = 500

            assertThrows<SocketTimeoutException> {
                client.getInputStream().read()
            }
        }

        // knio
        createKnioSocket(isSSL).use { client ->
            client.setReadTimeout(500)

            assertThrows<SocketTimeoutException> {
                client.getInputStream().read()
            }
        }
    }

    // XXX: How do I test write timeout?

    override suspend fun startReverseServer(isSSL: Boolean): AcceptOnlyServer {
        val server = AcceptOnlyServer(createJavaServerSocket(isSSL))
        server.start()

        return server
    }
}