package org.ivcode.knio.net.ssl

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ivcode.knio.nio.readSuspend
import org.ivcode.knio.nio.writeSuspend
import org.ivcode.knio.utils.compactOrIncreaseSize
import org.ivcode.knio.context.KnioContext
import org.ivcode.knio.io.KInputStream
import org.ivcode.knio.io.KOutputStream
import org.jetbrains.annotations.Blocking
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.ClosedChannelException
import javax.net.ssl.*
import kotlin.math.min

internal class KSSLSocketImpl (
    channel: AsynchronousSocketChannel,
    sslEngine: SSLEngine,
    useClientMode: Boolean,
    private val context: KnioContext
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
        networkRead = context.byteBufferPool.acquire(sslEngine.session.packetBufferSize)
        networkWrite = context.byteBufferPool.acquire(sslEngine.session.packetBufferSize)
        applicationRead = context.byteBufferPool.acquire(sslEngine.session.applicationBufferSize)
    }

    private val inputStream = object : KInputStream(context) {

        @Blocking
        override suspend fun read(b: ByteBuffer): Int {
            @Suppress("BlockingMethodInNonBlockingContext")
            return this@KSSLSocketImpl.read(b)
        }

        @Blocking
        override suspend fun close() {
            @Suppress("BlockingMethodInNonBlockingContext")
            this@KSSLSocketImpl.shutdownInput()
        }
    }

    private val outputStream = object : KOutputStream() {

        @Blocking
        override suspend fun write(b: ByteBuffer) {
            @Suppress("BlockingMethodInNonBlockingContext")
            this@KSSLSocketImpl.write(b)
        }

        @Blocking
        override suspend fun close() {
            @Suppress("BlockingMethodInNonBlockingContext")
            this@KSSLSocketImpl.shutdownOutput()
        }
    }

    override fun getInputStream(): KInputStream = inputStream
    override fun getOutputStream(): KOutputStream = outputStream

    @Blocking
    override suspend fun startHandshake() = handshakeMutex.withLock {
        startHandshake0()
    }


    @Blocking
    private suspend fun startHandshake0() {
        if(isHandshakeCompleted) return

        @Suppress("BlockingMethodInNonBlockingContext")
        sslEngine.beginHandshake()

        networkRead!!.clear().flip()
        networkWrite!!.clear()

        while (sslEngine.isHandshakeRequired) {
            when(sslEngine.handshakeStatus!!) {
                SSLEngineResult.HandshakeStatus.NEED_TASK -> {
                    runHandshakeTasks()
                }
                SSLEngineResult.HandshakeStatus.NEED_WRAP -> {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    wrapHandshake()
                }
                SSLEngineResult.HandshakeStatus.NEED_UNWRAP,
                SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN-> {
                    @Suppress("BlockingMethodInNonBlockingContext")
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

    @Blocking
    private suspend fun wrapHandshake() {
        val dummyBuffer = ByteBuffer.allocate(0)

        while (true) {
            networkWrite!!.clear()

            @Suppress("BlockingMethodInNonBlockingContext")
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
                        context.byteBufferPool
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

    @Blocking
    private suspend fun unwrapHandshake() {
        val dummyBuffer = ByteBuffer.allocate(0)

        while (true) {
            // try to unwrap data from the network buffer
            @Suppress("BlockingMethodInNonBlockingContext")
            val result = sslEngine.unwrap(networkRead, dummyBuffer)

            when (result.status!!) {
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    // An underflow implies there wasn't enough information in the network buffer to unwrap

                    // increase the available network buffer size
                    networkRead = networkRead!!.compactOrIncreaseSize(
                        sslEngine.session.packetBufferSize,
                        context.byteBufferPool
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

    private val SSLEngine.isHandshakeRequired: Boolean
        get() = this.handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED
                && this.handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING


    override suspend fun isInputShutdown(): Boolean {
        return isInputShutdown
    }

    override suspend fun isOutputShutdown(): Boolean {
        return isOutputShutdown
    }

    @Blocking
    override suspend fun shutdownInput() {
        val netBuff = networkRead ?: return
        networkRead = null

        try {
            try {
                @Suppress("BlockingMethodInNonBlockingContext")
                sslEngine.closeInbound()
            } catch (e: SSLException) {
                // ignore
            }

            // Clear buffer for reuse or release
            netBuff.clear()
        } finally {
            isInputShutdown = true
            context.byteBufferPool.release(netBuff)
        }
    }

    @Blocking
    override suspend fun shutdownOutput() {
        var netBuff = networkWrite ?: return
        networkWrite = null

        try {
            sslEngine.closeOutbound()

            netBuff.clear()
            out@ while (true) {
                @Suppress("BlockingMethodInNonBlockingContext")
                val result = sslEngine.wrap(ByteBuffer.allocate(0), netBuff)

                when (result.status!!) {

                    SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                        // increase network buffer size
                        netBuff = netBuff.compactOrIncreaseSize(
                            sslEngine.session.packetBufferSize,
                            context.byteBufferPool
                        )
                    }

                    SSLEngineResult.Status.OK -> {
                        try {
                            netBuff.flip()
                            while (netBuff.hasRemaining()) {
                                var written = 0;
                                repeat(3) { attempt ->
                                    written = ch.writeSuspend(netBuff)
                                    if (written > 0) return@repeat
                                    delay(100L * attempt) // Backoff delay
                                }

                                if (written <= 0) {
                                    break@out
                                }
                            }
                            netBuff.clear()
                            break
                        } catch (e: ClosedChannelException) {
                            // ignore
                        } catch (e: IOException) {
                            throw e
                        }
                    }

                    SSLEngineResult.Status.CLOSED -> {
                        // closed
                        break@out
                    }

                    else -> {
                        throw SSLException("Unexpected SSL wrap status: ${result.status}")
                    }
                }
            }

            try {
                @Suppress("BlockingMethodInNonBlockingContext")
                ch.shutdownOutput()
            } catch (e: ClosedChannelException) {
                // ignore
            } catch (e: IOException) {
                throw e
            }
        } finally {
            isOutputShutdown = true
            context.byteBufferPool.release(netBuff)
        }
    }

    @Blocking
    private suspend fun read(b: ByteBuffer): Int {
        if(!isHandshakeCompleted) {
            @Suppress("BlockingMethodInNonBlockingContext")
            startHandshake()
        }

        var app = this@KSSLSocketImpl.applicationRead ?: return -1
        var net = this@KSSLSocketImpl.networkRead ?: return -1

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

                    @Suppress("BlockingMethodInNonBlockingContext")
                    val result = sslEngine.unwrap(net, app)
                    when (result.status!!) {
                        SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                            networkRead = net.compactOrIncreaseSize(sslEngine.session.packetBufferSize, context.byteBufferPool)
                            net = networkRead!!
                        }
                        SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                            applicationRead = app.compactOrIncreaseSize(sslEngine.session.applicationBufferSize, context.byteBufferPool)
                            app = applicationRead!!
                        }
                        SSLEngineResult.Status.OK -> {
                            app.flip()
                            break
                        }
                        SSLEngineResult.Status.CLOSED -> {
                            @Suppress("BlockingMethodInNonBlockingContext")
                            shutdownInput()
                            break@input
                        }
                    }
                }
            } else {
                net.clear()
                val count = ch.readSuspend(net)
                if(count == -1) {
                    @Suppress("BlockingMethodInNonBlockingContext")
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

        return if(b.position() == start) {
            if(isInputShutdown) -1 else 0
        } else {
            b.position() - start
        }
    }

    @Blocking
    private suspend fun write(b: ByteBuffer) {
        if(!isHandshakeCompleted) {
            @Suppress("BlockingMethodInNonBlockingContext")
            startHandshake()
        }

        while(b.hasRemaining()) {
            @Suppress("BlockingMethodInNonBlockingContext")
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
                        context.byteBufferPool
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