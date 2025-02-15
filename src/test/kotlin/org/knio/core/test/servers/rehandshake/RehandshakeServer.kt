package org.knio.core.test.servers.rehandshake

import org.knio.core.net.ssl.KSSLSocket
import org.knio.core.test.servers.TestServer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.SocketException
import java.nio.ByteBuffer
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLSocket

/**
 * A server to test re-handshaking.
 */
class RehandshakeServer(
    private val serverSocket: SSLServerSocket
): TestServer, Runnable {

    override fun run() {
        while(!serverSocket.isClosed) {
            try {
                runClient(serverSocket.accept() as SSLSocket).start()
            } catch (e: SocketException) {
                // socket closed, probably
            }
        }
    }

    private fun runClient(client: SSLSocket) = Thread {
        client.use {
            val inputStream = client.inputStream
            try {
                val count = client.readInt()

                for (i in 0 until count) {
                    if(client.isClosed) {
                        break
                    }

                    if(i>0) {
                        // handshake
                        client.startHandshake()
                    }

                    val str = client.read()
                    client.write(str)

                    if(i<count-1) {
                        client.session.invalidate()
                    }
                }
            } catch (e: IOException) {
                println(e)
            }

            val read = inputStream.read()
            if(read!=-1) {
                throw IOException()
            }
        }
    }.apply {
        isDaemon = true
    }

    override suspend fun start(): TestServer {
        Thread(this).start()
        return this
    }

    override suspend fun stop() {
        serverSocket.close()
    }

    override fun isSSL(): Boolean {
        return true
    }

    override fun getPort(): Int {
        return serverSocket.localPort
    }

    fun SSLSocket.write(str: String) {
        write(str.length)

        val data = str.toByteArray(Charsets.UTF_8)
        var bytes = 0
        val lengthMax = 1024

        while(bytes<data.size) {
            val len = lengthMax.coerceAtMost(data.size - bytes)
            outputStream.write(data, bytes, len)
            bytes += len
        }
    }

    fun SSLSocket.write(value: Int) {
        getOutputStream().write(toBytes(value))
    }

    suspend fun KSSLSocket.write(str: String) {
        write(str.length)

        val data = str.toByteArray(Charsets.UTF_8)
        var bytes = 0
        val lengthMax = 1024

        while(bytes<data.size) {
            val len = lengthMax.coerceAtMost(data.size - bytes)
            getOutputStream().write(data, bytes, len)
            bytes += len
        }
    }

    suspend fun KSSLSocket.write(value: Int) {
        getOutputStream().write(toBytes(value))
    }

    fun SSLSocket.read(): String {
        val size = readInt()

        val buffer = ByteArray(size)
        getInputStream().read(buffer)

        return String(buffer, Charsets.UTF_8)
    }

    fun SSLSocket.readInt(): Int {
        val sizeBuffer = ByteArray(Int.SIZE_BYTES)

        var read = 0
        while(read<Int.SIZE_BYTES) {
            val r = getInputStream().read(sizeBuffer, read, Int.SIZE_BYTES-read)
            if(r==-1) {
                throw IOException()
            }
            read += r
        }

        return toInt(sizeBuffer)
    }

    suspend fun KSSLSocket.read(): String {
        val size = readInt()

        val buffer = ByteArray(size)
        getInputStream().read(buffer)

        return String(buffer, Charsets.UTF_8)
    }

    suspend fun KSSLSocket.readInt(): Int {
        val sizeBuffer = ByteArray(Int.SIZE_BYTES)

        var read = 0
        while(read<Int.SIZE_BYTES) {
            val r = getInputStream().read(sizeBuffer, read, Int.SIZE_BYTES-read)
            if(r==-1) {
                throw IOException()
            }
            read += r
        }

        return toInt(sizeBuffer)
    }

    private fun toBytes(value: Int): ByteArray {
        return ByteBuffer.allocate(Int.SIZE_BYTES).putInt(value).array()
    }

    private fun toInt(bytes: ByteArray): Int {
        require(bytes.size == Int.SIZE_BYTES)
        return ByteBuffer.wrap(bytes).int
    }
}