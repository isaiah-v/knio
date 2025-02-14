package org.knio.core.net.ssl

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import org.knio.core.nio.readSuspend
import org.knio.core.nio.writeSuspend
import org.knio.core.context.KnioContext
import org.knio.core.context.ReleasableBuffer
import org.knio.core.context.acquireReleasableByteBuffer
import org.knio.core.io.KInputStream
import org.knio.core.io.KOutputStream
import java.io.IOException
import java.net.SocketException
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

    private var isInputShutdown = false
    private val networkRead = ReadWriteBuffer(context.byteBufferPool.acquireReleasableByteBuffer(sslEngine.session.packetBufferSize).apply { value.limit(0) })
    private val application = ReadWriteBuffer(context.byteBufferPool.acquireReleasableByteBuffer(sslEngine.session.applicationBufferSize).apply { value.limit(0) })

    private var isOutputShutdown = false
    private var networkWrite = ReadWriteBuffer(context.byteBufferPool.acquireReleasableByteBuffer(sslEngine.session.packetBufferSize).apply { value.limit(0) })

    private val inputStream = object : KInputStream(context) {

        override suspend fun read(b: ByteBuffer): Int {
            return this@KSSLSocketImpl.read(b)
        }

        override suspend fun close() {
            this@KSSLSocketImpl.close()
        }
    }

    private val outputStream = object : KOutputStream() {
        override suspend fun write(b: ByteBuffer) {
            this@KSSLSocketImpl.write(b)
        }

        override suspend fun close() {
            this@KSSLSocketImpl.close()
        }
    }

    override suspend fun getInputStream(): KInputStream = lock.withLock {
        if(!ch.isOpen) {
            throw SocketException("Socket is closed")
        }
        if(isInputShutdown) {
            throw SocketException("Socket input is shutdown")
        }
        return inputStream
    }

    override suspend fun getOutputStream(): KOutputStream = lock.withLock {
        if(isOutputShutdown) {
            throw SocketException("Socket output is shutdown")
        }
        return outputStream
    }

    override suspend fun softStartHandshake() {
        if(!sslEngine.session.isValid) {
            startHandshake0()
        }
    }

    override suspend fun startHandshake() = lock.withLock {
        // initiates or renegotiates the SSL handshake
        startHandshake0()
    }

    /**
     * Same as [KSSLSocket.startHandshake] except that this is an internal function that executes without
     * acquiring the lock.
     *
     * @see [KSSLSocket.startHandshake]
     */
    private suspend fun startHandshake0() {
        @Suppress("BlockingMethodInNonBlockingContext")
        sslEngine.beginHandshake()
        handleHandshake0()
    }

    /**
     * Handles the handshake process.
     *
     * A handshake may be initiated at any time, and may be initiated multiple times. This method will handle processing
     * the handshake until it is complete.
     */
    private suspend fun handleHandshake0() {
        if(!sslEngine.isHandshaking) {
            return
        }

        var handshakeSession: SSLSession? = null

        while (sslEngine.isHandshaking) {
            if(handshakeSession == null) {
                val session = sslEngine.handshakeSession
                if(session != null) {
                    handshakeSession = session
                    initBuffersForHandshake(session)
                }
            }

            handshakeIteration0()
        }

        if(handshakeSession != null) {
            super.triggerHandshakeCompletion(handshakeSession)
        }
    }

    private suspend fun initBuffersForHandshake(session: SSLSession) {
        if(networkRead.value.capacity()<session.packetBufferSize) {
            networkRead.releasable.resize(session.packetBufferSize)
        }
        if(networkWrite.value.capacity()<session.packetBufferSize) {
            networkWrite.releasable.resize(session.packetBufferSize)
        }
        if(application.value.capacity()<session.applicationBufferSize) {
            application.releasable.resize(session.applicationBufferSize)
        }
    }

    private suspend fun handshakeIteration0() {
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
            }
        }
    }

    private fun runHandshakeTasks() {
        while (true) {
            val task = sslEngine.delegatedTask ?: break
            task.run()
        }
    }

    private suspend fun wrapHandshake() {
        wrap(src = ByteBuffer.wrap(ByteArray(0)))
    }

    private suspend fun wrap (
        src: ByteBuffer,
        dst: ReadWriteBuffer = networkWrite
    ) {
        while (true) {
            @Suppress("BlockingMethodInNonBlockingContext")
            val result = sslEngine.wrap(src, dst.write)

            when (result.status!!) {
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    // Dummy buffer used. If thrown, bad assumptions made
                    throw SSLException("Buffer underflow while wrapping in handshake")
                }

                SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                    handleOverflow(dst.writeReleasable)
                }

                SSLEngineResult.Status.OK -> {
                    // Unwrap was successful. Write the data to the channel.
                    dst.toMode(ReadWriteBuffer.Mode.READ)
                    while (dst.value.hasRemaining()) {
                        val read = ch.writeSuspend(dst.value, getWriteTimeout())
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

    private suspend fun unwrapHandshake() {

        while (true) {
            // try to unwrap data from the network buffer

            @Suppress("BlockingMethodInNonBlockingContext")
            val result = sslEngine.unwrap(networkRead.read, application.write)

            when (result.status!!) {
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    // If there's no room to read, increase buffer size
                    if (!networkRead.write.hasRemaining()) {
                        val buffer = networkRead.releasable
                        buffer.resize(buffer.value.capacity() + sslEngine.session.packetBufferSize)
                    }

                    // Read more data from the channel
                    val count = ch.readSuspend(networkRead.write, getReadTimeout())
                    if (count == -1) {
                        throw SSLException("Connection closed during handshake")
                    }
                    if (count == 0) {
                        throw SSLException("?? no data read during handshake. try again or error ??")
                    }
                }

                SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                    handleOverflow(networkRead.writeReleasable)
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

    private fun handleOverflow(buffer: ReleasableBuffer<ByteBuffer>) {
        val limit = buffer.value.limit()
        val capacity = buffer.value.capacity()

        if (limit == capacity) {
            buffer.resize(capacity + sslEngine.session.packetBufferSize)
        } else {
            buffer.value.limit(capacity)
        }
    }

    override suspend fun isInputShutdown(): Boolean = lock.withLock {
        return isInputShutdown
    }

    override suspend fun isOutputShutdown(): Boolean = lock.withLock {
        return isOutputShutdown
    }

    override suspend fun shutdownInput() = lock.withLock {
        shutdownInput0()
    }

    private suspend fun shutdownInput0() {
        if(networkRead.releasable.released) {
            return
        }

        try {
            try {
                @Suppress("BlockingMethodInNonBlockingContext")
                sslEngine.closeInbound()
            } catch (e: SSLException) {
                // ignore
            }

            // Clear buffer for reuse or release
            networkRead.value.clear()
        } finally {
            isInputShutdown = true
            networkRead.releasable.release()
        }
    }

    override suspend fun shutdownOutput() = lock.withLock {
        shutdownOutput0()
    }

    private suspend fun shutdownOutput0() {
        try {
            sslEngine.closeOutbound()

            out@ while (true) {
                @Suppress("BlockingMethodInNonBlockingContext")
                val result = sslEngine.wrap(ByteBuffer.allocate(0), networkWrite.write)

                when (result.status!!) {

                    SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                        handleOverflow(networkWrite.writeReleasable)
                    }

                    SSLEngineResult.Status.OK -> {
                        try {
                            networkWrite.toMode(ReadWriteBuffer.Mode.READ)
                            while (networkWrite.value.hasRemaining()) {
                                var written = 0
                                repeat(3) { attempt ->
                                    written = ch.writeSuspend(networkWrite.value)
                                    if (written > 0) return@repeat
                                    delay(100L * attempt) // Backoff delay
                                }

                                if (written <= 0) {
                                    break@out
                                }
                            }
                            networkWrite.value.clear()
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
            networkWrite.releasable.release()
        }
    }

    private suspend fun read(b: ByteBuffer): Int  = lock.withLock {
        read0(b)
    }


    /**
     * @implNote The `application` buffer must be empty or in a "read state" when exiting this method
     *
     * Buffer States:
     *  - Undefined:   Buffer Empty
     *  - Read State:  Bytes are read FROM the buffer
     *  - Write State: Bytes are written TO the buffer
     */
    private suspend fun read0(b: ByteBuffer): Int {
        if(isInputShutdown && !application.read.hasRemaining()) {
            return -1
        }

        if (application.releasable.released || networkRead.releasable.released) {
            return -1
        }
        val app = application
        val net = networkRead

        if(!sslEngine.session.isValid) {
            startHandshake0() // <-- flips application buffer to read mode
        }

        val start = b.position()

        input@ while(b.hasRemaining()) {
            application.toMode(ReadWriteBuffer.Mode.READ)

            // Add remaining application data to the buffer
            if(app.value.hasRemaining()) {
                val count = min(app.value.remaining(), b.remaining())
                b.put(b.position(), app.value, app.value.position(), count)

                app.value.position(app.value.position() + count)
                b.position(b.position() + count)

                continue
            }

            // Check if we're handshaking (could be initiated at any time, any number of times)
            if(sslEngine.isHandshaking) {
                handleHandshake0()
                continue@input
            }

            if(net.value.hasRemaining()) {

                while(true) {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    val result = sslEngine.unwrap(net.read, app.write)

                    when (result.status!!) {
                        SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                            // If there's no room to read, increase buffer size
                            if(!net.write.hasRemaining()) {
                                val buffer = net.releasable
                                buffer.resize(buffer.value.capacity() + sslEngine.session.packetBufferSize)
                            }
                        }
                        SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                            handleOverflow(app.writeReleasable)
                        }
                        SSLEngineResult.Status.OK -> {
                            break
                        }
                        SSLEngineResult.Status.CLOSED -> {
                            shutdownInput0()
                            break@input
                        }
                    }
                }
            } else {
                val count = ch.readSuspend(net.write, getReadTimeout())
                if(count == -1) {
                    shutdownInput0()
                    break@input
                }
                if (count == 0) {
                    // return if no data read
                    break@input
                }
            }
        }

        // In all cases the method exists with the application buffer in read mode or empty

        return if(b.position() == start) {
            if(isInputShutdown) -1 else 0
        } else {
            b.position() - start
        }
    }

    private suspend fun write(b: ByteBuffer) = lock.withLock {
        write0(b)
    }

    private suspend fun write0(b: ByteBuffer) {
        if(!sslEngine.session.isValid) {
            startHandshake0()
        }

        while(b.hasRemaining()) {

            // Check if we're handshaking (could be initiated at any time, any number of times)
            if(sslEngine.isHandshaking) {
                handleHandshake0()
                continue
            }

            wrap(src = b)
        }
    }

    /**
     * Returns true if the SSLEngine is handshaking.
     */
    private val SSLEngine.isHandshaking: Boolean
        get() = this.handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED
                && this.handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING


    /**
     * Keeps track of the read/write state of the buffer.
     *
     * - *Read Mode* is defined as the state in which data is read from this buffer
     * - *Write Mode* is defined as the state in which data is written into this buffer
     */
    private class ReadWriteBuffer(
        val releasable: ReleasableBuffer<ByteBuffer>,
        var mode: Mode = Mode.READ
    ) {
        enum class Mode {
            READ, WRITE;

            fun isRead() = this === READ
            fun isWrite() = this === WRITE
        }

        /** Returns the buffer */
        val value: ByteBuffer get() = releasable.value

        /** Returns the buffer if in read-mode, otherwise throws an exception */
        val read: ByteBuffer get() {
            toMode(Mode.READ)
            return releasable.value
        }

        val readReleasable: ReleasableBuffer<ByteBuffer> get() {
            toMode(Mode.READ)
            return releasable
        }

        /** Returns the buffer if in write-mode, otherwise throws an exception */
        val write: ByteBuffer get() {
            toMode(Mode.WRITE)
            return releasable.value
        }

        val writeReleasable: ReleasableBuffer<ByteBuffer> get() {
            toMode(Mode.WRITE)
            return releasable
        }

        /**
         * Swaps the buffer between read and write mode, preparing it for the opposite operation.
         */
        fun swap(): ByteBuffer {
            val buffer = releasable.value

            if(mode.isRead()) {
                mode = Mode.WRITE

                return buffer.compact()
            } else {
                mode = Mode.READ

                return buffer.flip()
            }
        }

        /**
         * Sets the buffer to the specified mode. If the buffer is already in the specified mode, this method does
         * nothing, otherwise it swaps the buffer to the opposite mode.
         */
        fun toMode(mode: Mode) {
            if(this.mode != mode) {
                swap()
            }
        }
    }

}