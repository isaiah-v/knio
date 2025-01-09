package org.ivcode.knio.net.ssl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ivcode.knio.io.KInputStream
import org.ivcode.knio.io.KOutputStream
import org.ivcode.knio.nio.readSuspend
import org.ivcode.knio.nio.writeSuspend
import org.ivcode.knio.system.ByteBufferPool
import org.ivcode.org.ivcode.knio.system.AsynchronousChannelFactory
import org.ivcode.org.ivcode.knio.system.AsynchronousChannelFactoryDefault
import org.ivcode.org.ivcode.knio.utils.compactOrIncreaseSize
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import javax.net.ssl.*
import kotlin.math.min


internal class KSSLSocketImpl (
    channel: AsynchronousSocketChannel = AsynchronousChannelFactory.getDefault().openAsynchronousSocketChannel(),
    sslEngine: SSLEngine,
    useClientMode: Boolean,
    private val bufferPool: ByteBufferPool = ByteBufferPool.getDefault()
): KSSLSocketAbstract(
    channel,
    sslEngine,
    useClientMode
) {

    private var handshakeMutex: Mutex = Mutex()
    private var isHandshakeCompleted = false

    private var isInputShutdown = false
    private var networkRead: ByteBuffer?
    private var applicationRead: ByteBuffer?

    private var isOutputShutdown = false
    private var networkWrite: ByteBuffer?

    init {
        networkRead = bufferPool.acquire(sslEngine.session.packetBufferSize)
        networkWrite = bufferPool.acquire(sslEngine.session.packetBufferSize)
        applicationRead = bufferPool.acquire(sslEngine.session.applicationBufferSize)
    }

    private val inputStream = object : KInputStream() {
        override suspend fun read(b: ByteBuffer): Int = this@KSSLSocketImpl.read(b)
        override suspend fun close() = this@KSSLSocketImpl.shutdownInput()
    }

    private val outputStream = object : KOutputStream() {
        override suspend fun write(b: ByteBuffer) = this@KSSLSocketImpl.write(b)
        override suspend fun close() = this@KSSLSocketImpl.shutdownOutput()
    }

    private suspend fun startHandshake0() = withContext(Dispatchers.IO) {
        if(isHandshakeCompleted) return@withContext

        sslEngine.beginHandshake()

        networkRead!!.clear().flip()
        networkWrite!!.clear()

        while (sslEngine.isHandshakeRequired) {
            when(sslEngine.handshakeStatus!!) {
                SSLEngineResult.HandshakeStatus.NEED_TASK -> {
                    runHandshakeTasks()
                }
                SSLEngineResult.HandshakeStatus.NEED_WRAP -> {
                    wrapHandshake()
                }
                SSLEngineResult.HandshakeStatus.NEED_UNWRAP,
                SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN-> {
                    unwrapHandshake()
                }
                SSLEngineResult.HandshakeStatus.FINISHED,
                SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING -> {
                    // DONE!
                    continue
                }
            }
        }

        // clear buffers
        //networkRead?.clear()
        networkWrite?.clear()
        applicationRead?.clear()?.flip()
        //applicationWrite?.clear()

        isHandshakeCompleted = true
    }

    private fun runHandshakeTasks() {
        while (true) {
            val task = sslEngine.delegatedTask ?: break
            task.run()
        }
    }

    private suspend fun wrapHandshake() = withContext(Dispatchers.IO) {
        val dummyBuffer = ByteBuffer.allocate(0)

        while (true) {
            networkWrite!!.clear()
            val result = sslEngine.wrap(dummyBuffer, networkWrite)

            when (result.status!!) {
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    //
                    throw SSLException("Buffer underflow while wrapping in handshake")
                }

                SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                    // Increase network buffer size. This shouldn't typically happen during handshake.
                    // The network buffer is clear and the size should be the same as the packet buffer size.
                    networkWrite = networkWrite!!.compactOrIncreaseSize(
                        sslEngine.session.packetBufferSize,
                        bufferPool
                    )
                }

                SSLEngineResult.Status.OK -> {
                    // Unwrap was successful. Write the data to the channel.
                    networkWrite!!.flip()
                    while (networkWrite!!.hasRemaining()) {
                        val read = ch.writeSuspend(networkWrite!!)
                        if (read == -1) {
                            throw SSLException("Connection closed during handshake")
                        }
                        if (read == 0) {
                            // TODO
                            throw SSLException("?? no data written during handshake. try again or error ??")
                        }
                    }
                    break
                }

                SSLEngineResult.Status.CLOSED -> {
                    // closed
                    throw SSLException("Connection closed during handshake")
                }
            }
        }
    }

    private suspend fun unwrapHandshake() = withContext(Dispatchers.IO) {
        val dummyBuffer = ByteBuffer.allocate(0)

        while (true) {
            // try to unwrap data from the network buffer
            val result = sslEngine.unwrap(networkRead, dummyBuffer)

            when (result.status!!) {
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    // An underflow implies there wasn't enough information in the network buffer to unwrap

                    // increase the available network buffer size
                    networkRead = networkRead!!.compactOrIncreaseSize(
                        sslEngine.session.packetBufferSize,
                        bufferPool
                    )

                    // read more data from the channel
                    val count = ch.readSuspend(networkRead!!)
                    if(count == -1) {
                        throw SSLException("Connection closed during handshake")
                    }
                    if(count == 0) {
                        // TODO
                        throw SSLException("?? no data read during handshake. try again or error ??")
                    }

                    // flip the buffer to prepare for unwrapping
                    networkRead!!.flip()
                }

                SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                    // shouldn't happen during handshake
                    throw SSLException("Buffer underflow while unwrapping in handshake")
                }

                SSLEngineResult.Status.OK -> {
                    // unwrap was successful. leave the data in the network buffer for the next unwrap
                    break
                }

                SSLEngineResult.Status.CLOSED -> {
                    // closed
                    throw SSLException("Connection closed during handshake")
                }
            }
        }
    }

    val SSLEngine.isHandshakeRequired: Boolean
        get() = this.handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED
                && this.handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING

    val SSLEngine.isHandshakeUnwrap: Boolean
        get() = this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP
                || this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN

    val SSLEngine.isHandshakeWrap: Boolean
        get() = this.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP

    override suspend fun startHandshake() = handshakeMutex.withLock {
        startHandshake0()
    }

    override fun getInputStream(): KInputStream = inputStream
    override fun getOutputStream(): KOutputStream = outputStream

    override suspend fun isInputShutdown(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun isOutputShutdown(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun shutdownInput() {
        this.networkRead = null
        this.applicationRead = null
    }

    override suspend fun shutdownOutput() {
        TODO("Not yet implemented")
    }

    private suspend fun read(b: ByteBuffer): Int = withContext(Dispatchers.IO) {
        if(!isHandshakeCompleted) {
            startHandshake()
        }

        var app = this@KSSLSocketImpl.applicationRead ?: return@withContext -1
        var net = this@KSSLSocketImpl.networkRead ?: return@withContext -1

        val start = b.position()

        input@ while(b.hasRemaining()) {
            if(app.hasRemaining()) {
                val count = min(app.remaining(), b.remaining())
                b.put(b.position(), app, app.position(), count)

                app.position(app.position() + count)
                b.position(b.position() + count)

                continue
            }

            if(net.hasRemaining()) {
                app.clear()
                while(true) {
                    val result = sslEngine.unwrap(net, app)
                    when (result.status!!) {
                        SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                            networkRead = net.compactOrIncreaseSize(sslEngine.session.packetBufferSize, bufferPool)
                            net = networkRead!!
                        }
                        SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                            applicationRead = app.compactOrIncreaseSize(sslEngine.session.applicationBufferSize, bufferPool)
                            app = applicationRead!!
                        }
                        SSLEngineResult.Status.OK -> {
                            app.flip()
                            break
                        }
                        SSLEngineResult.Status.CLOSED -> {
                            shutdownInput()
                            break@input
                        }
                    }
                }
            } else {
                net.clear()
                val count = ch.readSuspend(net)
                if(count == -1) {
                    shutdownInput()
                    break@input
                }
                if (count == 0) {
                    // return if no data read
                    break@input
                }
                net.flip()
            }
        }

        return@withContext if(b.position() == start) {
            if(isInputShutdown) -1 else 0
        } else {
            b.position() - start
        }
    }

    private suspend fun write(b: ByteBuffer) = withContext(Dispatchers.IO) {
        if(!isHandshakeCompleted) {
            startHandshake()
        }

        while(b.hasRemaining()) {
            val result = sslEngine.wrap(b, networkWrite)

            when (result.status!!) {
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    // increase network buffer size
                    throw SSLException("Buffer underflow while wrapping")
                }

                SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                    // increase network buffer size
                    networkWrite = networkWrite!!.compactOrIncreaseSize(
                        sslEngine.session.packetBufferSize,
                        bufferPool
                    )
                }

                SSLEngineResult.Status.OK -> {
                    networkWrite!!.flip()
                    while (networkWrite!!.hasRemaining()) {
                        val written = ch.writeSuspend(networkWrite!!)
                        if (written == -1) {
                            throw SSLException("Connection closed during handshake")
                        }
                        if (written == 0) {
                            // TODO
                            throw SSLException("?? no data written during handshake. try again or error ??")
                        }
                    }
                    networkWrite!!.clear()
                    break
                }

                SSLEngineResult.Status.CLOSED -> {
                    // closed
                    throw SSLException("Connection closed during handshake")
                }
            }
        }
    }
}