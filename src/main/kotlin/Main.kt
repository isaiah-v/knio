package org.ivcode

import kotlinx.coroutines.*
import org.ivcode.knio.lang.use
import org.ivcode.knio.net.ssl.KSSLSocket
import org.ivcode.knio.net.ssl.getKnioSSLSocketFactory
import org.ivcode.org.ivcode.knio.utils.createTrustAllSSLContext
import org.jetbrains.annotations.Blocking

import java.nio.ByteBuffer

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
suspend fun main() = coroutineScope {

    createTrustAllSSLContext().getKnioSSLSocketFactory().createSocket("localhost", 8080).use { socket ->

        socket.startHandshake()

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
