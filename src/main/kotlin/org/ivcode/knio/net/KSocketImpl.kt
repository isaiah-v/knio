package org.ivcode.knio.net

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ivcode.knio.nio.writeSuspend
import org.ivcode.knio.net.KSocketAbstract
import org.ivcode.knio.net.KSocketOutputStream
import org.jetbrains.annotations.Blocking
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.atomic.AtomicBoolean

internal class KSocketImpl internal constructor(
    /** The underlying AsynchronousSocketChannel. */
    channel: AsynchronousSocketChannel
): KSocketAbstract(channel) {

    /** The input shutdown flag. */
    private var isInputShutdown = AtomicBoolean(false)

    /** The output shutdown flag. */
    private var isOutputShutdown = AtomicBoolean(false)

    /** The input stream of the socket. */
    private val inputStream = object : KSocketInputStream() {
        /** read mutex */
        private val readMutex = Mutex()

        /**
         * Reads data into the provided ByteBuffer.
         *
         * @param b The ByteBuffer to read data into.
         * @return The number of bytes read.
         */
        override suspend fun read(b: ByteBuffer): Int = readMutex.withLock {
            return read0(b)
        }

        private suspend fun read0(b: ByteBuffer): Int {
            var total = 0

            while (b.hasRemaining()) {
                repeat(3) {
                    val result = read(b)

                    if (result == -1) {
                        // Blocking
                        close()
                        return -1
                    } else if (result > 0) {
                        total += result
                        return@repeat
                    }
                }
            }

            return total
        }

        /**
         * Closes the input stream.
         */
        @Blocking
        override suspend fun close() {
            @Suppress("BlockingMethodInNonBlockingContext")
            this@KSocketImpl.shutdownInput()
        }
    }

    /** The output stream of the socket. */
    private val outputStream = object : KSocketOutputStream() {
        /** write mutex */
        private val writeMutex = Mutex()

        /**
         * Writes data from the provided ByteBuffer.
         *
         * @param b The ByteBuffer containing data to write.
         */
        @Blocking
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
        @Blocking
        override suspend fun close() {
            @Suppress("BlockingMethodInNonBlockingContext")
            this@KSocketImpl.shutdownOutput()
        }
    }



    /**
     * Gets the input stream of the socket.
     *
     * @return The KInputStream.
     */
    override fun getInputStream(): KSocketInputStream = this.inputStream



    /**
     * Gets the output stream of the socket.
     *
     * @return The KOutputStream.
     */
    override fun getOutputStream(): KSocketOutputStream = outputStream

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
    @Blocking
    @Throws(IOException::class)
    override suspend fun shutdownInput() {
        if(!isInputShutdown.getAndSet(true)) {
            @Suppress("BlockingMethodInNonBlockingContext")
            ch.shutdownInput()
        }
    }


    /**
     * Shuts down the output side of the socket.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Blocking
    @Throws(IOException::class)
    override suspend fun shutdownOutput() {
        if(!isOutputShutdown.getAndSet(true)) {
            @Suppress("BlockingMethodInNonBlockingContext")
            ch.shutdownOutput()
        }
    }
}