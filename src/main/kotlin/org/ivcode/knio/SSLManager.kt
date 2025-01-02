package org.ivcode.knio

import org.ivcode.knio.utils.*
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult
import javax.net.ssl.SSLException

/**
 * Manages SSL/TLS connections, including the handshake process and data encryption/decryption.
 *
 * @property useClientMode Indicates if the SSL engine should operate in client mode.
 * @param sslContext The SSL context used to create the SSL engine.
 */
class SSLManager (
    private val useClientMode: Boolean,
    sslContext: SSLContext
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(SSLManager::class.java)
    }

    /** The SSL engine for the connection */
    private var sslEngine: SSLEngine = sslContext.createSSLEngine().apply { useClientMode = this@SSLManager.useClientMode }

    /** The network buffer for reading data from the network */
    private var netRBuffer: BufferPooled = BufferPooled(sslEngine.session.packetBufferSize).apply { flip() }

    /** The network buffer for writing data to the network */
    private var netWBuffer: BufferPooled = BufferPooled(sslEngine.session.packetBufferSize)

    /** The application buffer for reading data from the application */
    var appBuffer: BufferPooled = BufferPooled(sslEngine.session.applicationBufferSize)

    /**
     * Begins the SSL handshake process.
     *
     * @return The interest operations for the selection key.
     */
    fun beginHandshake(key: EventKey) {
        LOGGER.info("Beginning SSL handshake")
        sslEngine.beginHandshake()

        if(useClientMode) {
            key.interestOps = SelectionKey.OP_WRITE
        } else {
            key.interestOps = SelectionKey.OP_READ
        }
    }

    /**
     * Handles SSL handshake events.
     *
     * @param key The selection key associated with the channel.
     * @param onFinish Optional callback to invoke when the handshake is finished.
     * @return True if the handshake is still in progress, false otherwise.
     */
    fun handshakeEvent(key: EventKey, onFinish: (()->Unit)?=null): Boolean {
        if(!isHandshake()) {
            return false
        }

        val context = HandshakeContext(key)
        LOGGER.info("Handling SSL handshake event")

        // If SSL handshake is not yet finished, continue processing it
        while (isHandshake()) {
            when (context.doHandshake()) {
                HandshakeResponse.CONTINUE_HANDSHAKE -> continue
                HandshakeResponse.RETURN_TO_EVENT_LOOP -> break
            }
        }

        if(isHandshake()) {
            return true
        } else {
            // clear the buffer after handshake is complete
            appBuffer.clear().flip()
            onFinish?.invoke()
            return false
        }
    }

    /**
     * Performs a single step of the SSL handshake process.
     *
     * @return The response indicating the next action to take.
     */
    private fun HandshakeContext.doHandshake(): HandshakeResponse {
        when (sslEngine.handshakeStatus) {
            SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
            SSLEngineResult.HandshakeStatus.FINISHED -> {
                // done (nothing more to process)
                LOGGER.info("SSL handshake finished")
                return HandshakeResponse.CONTINUE_HANDSHAKE
            }

            SSLEngineResult.HandshakeStatus.NEED_TASK -> {
                LOGGER.info("SSL handshake needs task")
                return doHandshakeTask()
            }

            SSLEngineResult.HandshakeStatus.NEED_WRAP -> {
                LOGGER.info("SSL handshake needs wrap")
                return doHandshakeWrap()
            }

            SSLEngineResult.HandshakeStatus.NEED_UNWRAP,
            SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN -> {
                LOGGER.info("SSL handshake needs unwrap")
                return doHandshakeUnwrap()
            }

            else -> throw IllegalStateException("Unexpected HandshakeStatus: ${sslEngine.handshakeStatus}")
        }
    }

    /**
     * Performs the delegated tasks required by the SSL handshake.
     *
     * @return The response indicating the next action to take.
     */
    private fun doHandshakeTask(): HandshakeResponse {
        while (true) {
            sslEngine.delegatedTask?.apply { run() } ?: break
        }

        LOGGER.info("SSL handshake task completed")
        return HandshakeResponse.CONTINUE_HANDSHAKE
    }

    /**
     * Performs the wrap operation for the SSL handshake.
     *
     * @return The response indicating the next action to take.
     */
    private fun HandshakeContext.doHandshakeWrap(): HandshakeResponse {
        // The SSL engine needs data wrapped (e.g., sending handshake data)
        if (!key.isWritable) {
            if (key.interestOps and SelectionKey.OP_WRITE == 0) {
                key.interestOps = key.interestOps or SelectionKey.OP_WRITE
            }

            // return to event-loop and wait for write event
            LOGGER.info("Returning to event loop for write event")
            return HandshakeResponse.RETURN_TO_EVENT_LOOP
        }

        while (sslEngine.handshakeStatus.isNeedWrap()) {
            netWBuffer.clear()  // Prepare network buffer for writing
            val result = sslEngine.wrap(ByteBuffer.wrap(ByteArray(0)), netWBuffer)
            netWBuffer.flip()  // Prepare for writing to the channel

            // return only if the wrap result is handled
            key.handleWrapResult(result)
        }

        LOGGER.info("SSL handshake wrap completed")
        return HandshakeResponse.CONTINUE_HANDSHAKE
    }

    /**
     * Handles the result of the wrap operation.
     *
     * @param result The result of the wrap operation.
     */
    private fun EventKey.handleWrapResult(result: SSLEngineResult) {
        val channel = this.channel as SocketChannel

        when (result.status!!) {
            SSLEngineResult.Status.OK -> {
                // Data successfully wrapped, send it to the client
                while (netWBuffer.hasRemaining()) {
                    channel.write(netWBuffer)
                }
                LOGGER.info("Data successfully wrapped and sent to client")
            }

            SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                increaseNetWriteBuffer()
                LOGGER.warn("Buffer overflow during wrap, increased buffer size")
            }

            SSLEngineResult.Status.CLOSED -> {
                // Connection closed by the client
                LOGGER.warn("Connection closed by client during wrap")
                throw ClosedSocketException("socket closed")
            }

            else -> throw IllegalStateException("Unexpected SSLEngineResult.Status: ${result.status}")
        }
    }

    /**
     * Performs the unwrap operation for the SSL handshake.
     *
     * @return The response indicating the next action to take.
     */
    private fun HandshakeContext.doHandshakeUnwrap(): HandshakeResponse {

        val channel = key.channel as SocketChannel
        while (sslEngine.handshakeStatus.isNeedUnwrap() || sslEngine.handshakeStatus.isNeedUnwrapAgain()) {
            if(netRBuffer.hasRemaining()) {
                appBuffer.clear()  // Prepare application buffer for writing
                val result = sslEngine.unwrap(netRBuffer, appBuffer)
                appBuffer.flip()  // Prepare for reading from the buffer

                handleUnwrapStatus(result)
                if(result.status.isOk()) {
                    // if okay, then we might not need to read, skip the read and check again
                    continue
                } else if (result.status.isBufferOverflow()) {
                    // if overflow, then the app buffer was too small. Adjustments have been made, so
                    // we can try again
                    continue
                }
            } else if(key.isReadable) {
                netRBuffer.clear()
            }

            if(!key.isReadable) {
                if(key.interestOps and SelectionKey.OP_READ == 0) {
                    key.interestOps = key.interestOps or SelectionKey.OP_READ
                }

                // return to event-loop and wait for read event
                LOGGER.info("Returning to event loop for read event")
                return HandshakeResponse.RETURN_TO_EVENT_LOOP
            }

            val bytesRead = channel.read(netRBuffer)
            if(bytesRead == -1) {
                // closed
                LOGGER.warn("Connection closed by client during unwrap")
                throw ClosedSocketException("socket closed")
            } else if(bytesRead == 0) {
                // all done
                netRBuffer.apply { position(limit()) }
                return HandshakeResponse.RETURN_TO_EVENT_LOOP
            } else {
                netRBuffer.flip()
            }
        }

        LOGGER.info("SSL handshake unwrap completed")
        return HandshakeResponse.CONTINUE_HANDSHAKE
    }

    /**
     * Handles the result of the unwrap operation.
     *
     * @param result The result of the unwrap operation.
     */
    private fun handleUnwrapStatus(result: SSLEngineResult) {
        when (result.status!!) {
            SSLEngineResult.Status.OK -> {
                // Data successfully unwrapped, continue handshake
                LOGGER.info("Data successfully unwrapped")
            }

            SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                // Need to read more data for unwrap
                if(netRBuffer.position() == 0) {
                    // if the position is 0, then we cannot compact. We need to resize the buffer
                    increaseNetReadBuffer()
                } else {
                    // if the position is not 0, then try to compact first
                    netRBuffer.compact()
                }
                LOGGER.warn("Buffer underflow during unwrap, increased buffer size")
            }

            SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                // Need to increase the application buffer size and try again
                if(appBuffer.position() == 0 && appBuffer.limit() == 0 ) {
                    appBuffer.clear()
                } else if(appBuffer.position() == 0) {
                    // if the position is 0, then we cannot compact. We need to resize the buffer
                    increaseAppBuffer()
                } else {
                    // if the position is not 0, then try to compact first
                    appBuffer.compact()
                }
                LOGGER.warn("Buffer overflow during unwrap, increased application buffer size")
            }

            SSLEngineResult.Status.CLOSED -> {
                // Connection closed by the client
                LOGGER.warn("Connection closed by client during unwrap")
                throw ClosedSocketException("socket closed")
            }

            else -> throw IllegalStateException("Unexpected SSLEngineResult.Status: ${result.status}")
        }
    }

    /**
     * Increases the size of the network write buffer.
     */
    private fun increaseNetWriteBuffer() {
        this.netWBuffer.resize(netWBuffer.capacity() + sslEngine.session.packetBufferSize)
    }

    /**
     * Increases the size of the network read buffer.
     */
    private fun increaseNetReadBuffer() {
        this.netRBuffer.resize(netRBuffer.capacity() + sslEngine.session.packetBufferSize)
    }

    /**
     * Increases the size of the application buffer.
     */
    private fun increaseAppBuffer() {
        this.appBuffer.resize(appBuffer.capacity() + sslEngine.session.applicationBufferSize)
    }

    /**
     * Checks if the SSL handshake is still in progress.
     *
     * @return True if the handshake is still in progress, false otherwise.
     */
    private fun isHandshake(): Boolean {
        return sslEngine.handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED &&
            sslEngine.handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING
    }

    /**
     * Context for managing the SSL handshake process.
     *
     * @property key The selection key associated with the channel.
     */
    private data class HandshakeContext (
        val key: EventKey
    )

    /**
     * Enum representing the possible responses from a handshake operation.
     */
    private enum class HandshakeResponse {
        /** Continue the handshake process */
        CONTINUE_HANDSHAKE,

        /** Return to the event loop */
        RETURN_TO_EVENT_LOOP,
    }

    /**
     * Gets the reader for the given selection key. This reader will unwrap the data from the network into clear text.
     *
     * This is to be used after the SSL handshake is complete.
     *
     * @param key The selection key associated with the channel.
     * @return The NioReader for reading unwrapped data.
     */
    fun getReader(key: EventKey): Reader {
        return object : Reader {
            override val buffer: Buffer get() = appBuffer
            override fun read(): Boolean = key.read()
        }
    }

    /**
     * Performs the read operation for the SSL connection. Data is read from the network and unwrapped into clear text
     * on to the application buffer.
     *
     * @return True if the read operation is complete, false otherwise.
     */
    private fun EventKey.read(): Boolean {
        val channel = this.channel as SocketChannel
        if(!isReadable) {
            LOGGER.warn("Attempt made to read from channel while not readable")
            return false
        }

        while (true) {
            if (netRBuffer.hasRemaining()) {

                val result = sslEngine.unwrap(netRBuffer, appBuffer)
                handleUnwrapStatus(result)
                if(result.status.isOk()) {
                    // if okay, then read is complete. return so that the app buffer can be processed
                    appBuffer.flip()
                    return true
                } else if (result.status.isBufferOverflow()) {
                    // if overflow, then the app buffer was too small. Adjustments have been made, so
                    // we can try again
                    continue
                }
            } else {
                // nothing to process, so clear buffer before reading more data
                netRBuffer.clear()
            }

            // read from the channel after we've determined that we need more data
            val bytes = channel.read(netRBuffer)
            netRBuffer.flip()

            if(bytes == -1) {
                // closed
                LOGGER.warn("Connection closed by client during read")
                throw ClosedSocketException("socket closed")
            } else if(bytes == 0) {
                // all done
                return false
            } else {
                // process the data
                continue
            }
        }
    }

    /**
     * Writes data to the SSL connection.
     *
     * @param key The selection key associated with the channel.
     * @param data The data to be written.
     */
    fun write(key: EventKey, data: ByteBuffer) {
        while (data.hasRemaining()) {
            netWBuffer.clear()
            val result = sslEngine.wrap(data, netWBuffer)
            netWBuffer.flip()

            key.handleWrapResult(result)
        }
        LOGGER.info("Data successfully written to SSL connection")
    }

    /**
     * Closes the SSL connection.
     *
     * @param key The selection key associated with the channel.
     */
    fun close(key: EventKey) {
        try {
            val channel = key.channel as SocketChannel

            // Initiate outbound close handshake
            LOGGER.info("Initiating SSL close handshake...")
            sslEngine.closeOutbound()
            while (!sslEngine.isOutboundDone) {
                netWBuffer.clear()
                val result = sslEngine.wrap(ByteBuffer.wrap(ByteArray(0)), netWBuffer)
                if (result.status == SSLEngineResult.Status.CLOSED) {
                    LOGGER.info("SSL outbound closed.")
                    break
                }

                netWBuffer.flip()
                while (netWBuffer.hasRemaining()) {
                    channel.write(netWBuffer)
                }
            }

            // Handle inbound close handshake
            LOGGER.info("Handling inbound close handshake...")
            try {
                sslEngine.closeInbound()
            } catch (e: SSLException) {
                println("SSLException during closeInbound: ${e.message}")
            }

            while (!sslEngine.isInboundDone) {
                if (channel.read(netRBuffer) <= 0) {
                    LOGGER.info("No more data from server.")
                    break
                }

                netRBuffer.flip()
                val result = sslEngine.unwrap(netRBuffer, appBuffer)
                netRBuffer.compact()
                if (result.status == SSLEngineResult.Status.CLOSED) {
                    LOGGER.info("SSL inbound closed.")
                    break
                }
            }
        } finally {
            netRBuffer.close()
            netWBuffer.close()
            appBuffer.close()
        }
    }
}