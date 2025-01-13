package org.ivcode

import org.ivcode.knio.lang.use
import kotlin.io.path.Path


/*
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
suspend fun main() = coroutineScope {

    launch {
        createSSLContext(keystore = "src/test/resources/keystore.p12", keystorePassword = "password")
            .getKnioSSLServerSocketFactory()
            .createServerSocket(8080)
            .use { serverSocket ->
                serverSocket.accept().use { socket ->

                    socket.getInputStream().use { inn ->
                        val buffer = ByteBuffer.allocate(10)
                        while (true) {
                            val read = inn.read(buffer)

                            if (read == -1) {
                                break
                            }
                            buffer.flip()
                            val byteArray = ByteArray(buffer.remaining())
                            buffer.get(byteArray)
                            print(String(byteArray))
                            buffer.clear()
                        }
                        println()
                    }

                    socket.getOutputStream().use { out ->
                        out.write("HTTP/1.1 200 OK\r\n".toByteArray())
                        out.write("Content-Length: 5\r\n".toByteArray())
                        out.write("\r\n".toByteArray())
                        out.write("Hello".toByteArray())
                        out.flush()
                    }
                }
            }
    }

    createTrustAllSSLContext().getKnioSSLSocketFactory().createSocket("localhost", 8080)
        .use { socket ->
        socket.getOutputStream().use { out ->
            out.write("GET / HTTP/1.1\r\n".toByteArray())
            out.write("Host: localhost\r\n".toByteArray())
            out.write("Connection: close\r\n".toByteArray())
            out.write("\r\n".toByteArray())
            out.flush()
        }

        socket.getInputStream().use { inn ->
            val buffer = ByteBuffer.allocate(10)
            while (true) {
                val read = inn.read(buffer)

                if (read == -1) {
                    break
                }
                buffer.flip()
                val byteArray = ByteArray(buffer.remaining())
                buffer.get(byteArray)
                print(String(byteArray))
                buffer.clear()
            }
            println()
        }
    }
}
*/



