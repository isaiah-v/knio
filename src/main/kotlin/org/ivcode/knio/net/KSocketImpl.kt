package org.ivcode.knio.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ivcode.knio.io.KInputStream
import org.ivcode.knio.io.KOutputStream
import org.ivcode.knio.nio.readSuspend
import org.ivcode.knio.nio.writeSuspend
import org.ivcode.org.ivcode.knio.net.KSocketAbstract
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.atomic.AtomicBoolean

internal class KSocketImpl internal constructor(
    /** The underlying AsynchronousSocketChannel. */
    channel: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
): KSocketAbstract(channel) {

    /** The input shutdown flag. */
    private var isInputShutdown = AtomicBoolean(false)

    /** The output shutdown flag. */
    private var isOutputShutdown = AtomicBoolean(false)

    /** The input stream of the socket. */
    private val inputStream = object : KInputStream() {
        /** read mutex */
        private val readMutex = Mutex()

        /**
         * Reads data into the provided ByteBuffer.
         *
         * @param b The ByteBuffer to read data into.
         * @return The number of bytes read.
         */
        override suspend fun read(b: ByteBuffer): Int = readMutex.withLock {
            // only one thread can read at a time to avoid ReadPendingException

            val result = channel.readSuspend(b, rTimeout)
            if (result == -1) {
                close()
            }

            result
        }

        /**
         * Closes the input stream.
         */
        override suspend fun close() = this@KSocketImpl.shutdownInput()
    }

    /** The output stream of the socket. */
    private val outputStream = object : KOutputStream() {
        /** write mutex */
        private val writeMutex = Mutex()

        /**
         * Writes data from the provided ByteBuffer.
         *
         * @param b The ByteBuffer containing data to write.
         */
        override suspend fun write(b: ByteBuffer):Unit = writeMutex.withLock {
            // only one thread can write at a time to avoid WritePendingException

            while(b.hasRemaining()) {
                val result = channel.writeSuspend(b, wTimeout)

                if (result == -1) {
                    close()
                    break
                }
            }
        }

        /**
         * Closes the output stream.
         */
        override suspend fun close() = this@KSocketImpl.shutdownOutput()
    }



    /**
     * Gets the input stream of the socket.
     *
     * @return The KInputStream.
     */
    override fun getInputStream(): KInputStream = this.inputStream



    /**
     * Gets the output stream of the socket.
     *
     * @return The KOutputStream.
     */
    override fun getOutputStream(): KOutputStream = outputStream

    /**
     * Checks if the input is shutdown.
     *
     * @return True if the input is shutdown, false otherwise.
     */
    override suspend fun isInputShutdown(): Boolean = isInputShutdown.get()

    /**
     * Checks if the output is shutdown.
     *
     * @return True if the output is shutdown, false otherwise.
     */
    override suspend fun isOutputShutdown(): Boolean = isOutputShutdown.get()

    /**
     * Shuts down the input side of the socket.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun shutdownInput():Unit = withContext(Dispatchers.IO) {
        if(!isInputShutdown.getAndSet(true)) {
            ch.shutdownInput()
        }
    }


    /**
     * Shuts down the output side of the socket.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Throws(IOException::class)
    override suspend fun shutdownOutput():Unit = withContext(Dispatchers.IO) {
        if(!isOutputShutdown.getAndSet(true)) {
            ch.shutdownOutput()
        }
    }
}