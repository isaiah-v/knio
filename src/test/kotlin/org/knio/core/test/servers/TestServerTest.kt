package org.knio.core.test.servers

import kotlinx.coroutines.runBlocking
import org.knio.core.net.KServerSocket
import org.knio.core.net.KServerSocketFactory
import org.knio.core.net.KSocket
import org.knio.core.net.KSocketFactory
import org.knio.core.net.ssl.KSSLSocket
import org.knio.core.net.ssl.getKnioSSLServerSocketFactory
import org.knio.core.net.ssl.getKnioSSLSocketFactory
import org.knio.core.test.utils.createTestSSLContext
import org.knio.core.test.utils.createTrustAllSSLContext
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import javax.net.ServerSocketFactory
import javax.net.SocketFactory
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLSocket

private const val PORT = 8080
private const val SSL_PORT = 8443

abstract class TestServerTest<T: TestServer> {

    /**
     * Starts the reverse server.
     *
     * @param isSSL Whether to use SSL.
     * @return The started reverse server.
     */
    protected abstract suspend fun startReverseServer(isSSL: Boolean): T


    /**
     * Runs the test server, starting and stopping it before and after the block.
     *
     * @param isSSL Whether to use SSL.
     * @param block The block to run.
     */
    protected fun runServer(isSSL: Boolean, block: suspend T.() -> Unit) = runBlocking {
        val server = startReverseServer(isSSL)
        try {
            server.block()
        } finally {
            server.stop()
        }
    }
}


fun createJavaServerSocket(isSSL: Boolean): ServerSocket = if(isSSL) {
    createTestSSLContext().serverSocketFactory.createServerSocket(SSL_PORT)
} else {
    ServerSocketFactory.getDefault().createServerSocket(PORT)
}

fun createJavaSSLServerSocket (
    protocol: String = "TLS"
): SSLServerSocket = createTestSSLContext(protocol).serverSocketFactory.createServerSocket(SSL_PORT) as SSLServerSocket


fun TestServer.createJavaSocket(): Socket = if(isSSL()) {
    createJavaSSLSocket()
} else {
    SocketFactory.getDefault().createSocket("localhost", getPort())
}

fun TestServer.createJavaSSLSocket(
    protocol: String = "TLS"
): SSLSocket =
    createTrustAllSSLContext(protocol).socketFactory.createSocket("localhost", getPort()) as SSLSocket


suspend fun createKnioServerSocket(isSSL: Boolean): KServerSocket = if(isSSL) {
    createTestSSLContext().getKnioSSLServerSocketFactory().createServerSocket(SSL_PORT)
} else {
    KServerSocketFactory.getDefault().createServerSocket(PORT)
}

suspend fun TestServer.createKnioSocket(): KSocket = if (isSSL()) {
    createTrustAllSSLContext().getKnioSSLSocketFactory().createSocket("localhost", getPort())
} else {
    KSocketFactory.getDefault().createSocket("localhost", getPort())
}

suspend fun TestServer.createKnioSSLSocket(
    protocol: String = "TLS"
): KSSLSocket =
    createTrustAllSSLContext(protocol).getKnioSSLSocketFactory().createSocket("localhost", getPort())


fun Socket.write(value: Int) {
    getOutputStream().write(toBytes(value))
}

suspend fun KSocket.write(str: String) {
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

suspend fun KSocket.write(value: Int) {
    getOutputStream().write(toBytes(value))
}

fun Socket.write(str: String) {
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


fun Socket.read(): String? {
    val size = readInt() ?: return null

    val buffer = ByteArray(size)

    var read = 0
    while(read<size) {
        val r = getInputStream().read(buffer, read, size-read)
        if(r==-1) {
            return null
        }

        read += r
    }

    return String(buffer, Charsets.UTF_8)
}


fun Socket.readInt(): Int? {
    val sizeBuffer = ByteArray(Int.SIZE_BYTES)

    var read = 0
    while(read<Int.SIZE_BYTES) {
        val r = getInputStream().read(sizeBuffer, read, Int.SIZE_BYTES-read)
        if(r==-1) {
            return null
        }
        read += r
    }

    return toInt(sizeBuffer)
}

suspend fun KSocket.readInt(): Int? {
    val sizeBuffer = ByteArray(Int.SIZE_BYTES)

    var read = 0
    while(read<Int.SIZE_BYTES) {
        val r = getInputStream().read(sizeBuffer, read, Int.SIZE_BYTES-read)
        if(r==-1) {
            return null
        }
        read += r
    }

    return toInt(sizeBuffer)
}

suspend fun KSocket.read(): String? {
    val size = readInt() ?: return null

    val buffer = ByteArray(size)

    var read = 0
    while(read<size) {
        val r = getInputStream().read(buffer, read, size-read)
        if(r==-1) {
            return null
        }

        read += r
    }

    return String(buffer, Charsets.UTF_8)
}

private fun toInt(bytes: ByteArray): Int {
    require(bytes.size == Int.SIZE_BYTES)
    return ByteBuffer.wrap(bytes).int
}

private fun toBytes(value: Int): ByteArray {
    return ByteBuffer.allocate(Int.SIZE_BYTES).putInt(value).array()
}