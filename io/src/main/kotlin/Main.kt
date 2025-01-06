package org.ivcode

import kotlinx.coroutines.*
import org.ivcode.knio.io.KInputStreamReader
import org.ivcode.knio.io.KBufferedReader
import org.ivcode.knio.lang.use
import org.ivcode.knio.net.KServerSocket
import org.ivcode.knio.net.KSocket
import java.net.InetSocketAddress


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
suspend fun main() = coroutineScope {
    launch {
        KServerSocket.open(8080).use { serverSocket ->
            val socket = serverSocket.accept()
            socket.use {
                val inputStream = it.getInputStream()
                val reader = KInputStreamReader(inputStream)
                val bufferedReader = KBufferedReader(reader)

                bufferedReader.use { r ->
                    var line: String? = r.readLine()   // read the first line using nio (suspends instead of blocking)
                    while (line != null) {
                        println(line)                   // print the line
                        line = r.readLine()            // read the next line using nio (suspends instead of blocking)
                    }
                } // close the bufferedReader and underlying reader and input stream

                it.getOutputStream().use {
                    it.write("HTTP/1.1 200 OK\r\n".toByteArray())
                    it.write("Content-Type: text/plain\r\n".toByteArray())
                    it.write("\r\n".toByteArray())
                    it.write("Hello, World!".toByteArray())
                }
            } // close the socket
        } // close the serverSocket
    }


    KSocket.open(InetSocketAddress("localhost", 8080)).use {
        val inputStream = it.getInputStream()
        val reader = KInputStreamReader(inputStream)
        val bufferedReader = KBufferedReader(reader)

        it.getOutputStream().use { out ->
            out.write("GET / HTTP/1.1\r\n".toByteArray())
            out.write("Host: localhost\r\n".toByteArray())
            out.write("\r\n".toByteArray())
        }

        bufferedReader.use { r ->
            var line: String? = r.readLine()   // read the first line using nio (suspends instead of blocking)
            while (line != null) {
                println(line)                   // print the line
                line = r.readLine()            // read the next line using nio (suspends instead of blocking)
            }
        } // close the bufferedReader and underlying reader and input stream
    }

    /*
    val path = Path.of("io/src/test/resources/test.txt")
    val inputStream = KFileInputStream(path)
    val reader = KInputStreamReader(inputStream)
    val bufferedReader = KBufferedReader(reader)

    bufferedReader.use {
        var line: String? = it.readLine()   // read the first line using nio (suspends instead of blocking)
        while (line != null) {
            println(line)                   // print the line
            line = it.readLine()            // read the next line using nio (suspends instead of blocking)
        }
    } // close the bufferedReader and underlying reader and input stream

     */
}
