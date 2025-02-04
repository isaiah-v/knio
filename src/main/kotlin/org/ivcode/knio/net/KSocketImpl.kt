package org.ivcode.knio.net

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ivcode.knio.context.KnioContext
import org.ivcode.knio.io.KInputStream
import org.ivcode.knio.io.KOutputStream
import org.ivcode.knio.nio.readSuspend
import org.ivcode.knio.nio.writeSuspend
import java.io.IOException
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.atomic.AtomicBoolean

internal class KSocketImpl internal constructor(
    /** The underlying AsynchronousSocketChannel. */
    channel: AsynchronousSocketChannel,
    context: KnioContext
): KSocketAbstract(channel) {

    /** The input shutdown flag. */
    private var isInputShutdown = AtomicBoolean(false)

    /** The output shutdown flag. */
    private var isOutputShutdown = AtomicBoolean(false)

    /** The input stream of the socket. */
    private val inputStream = object : KInputStream(context) {
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
            if(isInputShutdown()) {
                return -1
            }

            var total = 0

            while (b.hasRemaining()) {
                val result = channel.readSuspend(b, getReadTimeout())

                if (result == -1) {
                    // Blocking
                    shutdownInput()
                    return -1
                } else if (result > 0) {
                    total += result
                    return total
                }
            }

            return total
        }

        /**
         * Closes the socket as per the [java.net.Socket.getInputStream] says.
         */
        override suspend fun close() {
            this@KSocketImpl.close()
        }
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
                val result = channel.writeSuspend(b, getWriteTimeout())

                if (result == -1) {
                    shutdownOutput()
                    break
                }
            }
        }

        /**
         * Closes the socket as per the [java.net.Socket.getOutputStream] says.
         */
        override suspend fun close() {
            this@KSocketImpl.close()
        }
    }




    /**
     * Gets the input stream of the socket.
     *
     * @return The KInputStream.
     */
    override suspend fun getInputStream(): KInputStream {
        if(!ch.isOpen) {
            throw SocketException("Socket is closed")
        }
        if(isInputShutdown()) {
            throw SocketException("Socket input is shutdown")
        }

        return this.inputStream
    }



    /**
     * Gets the output stream of the socket.
     *
     * @return The KOutputStream.
     */
    override suspend fun getOutputStream(): KOutputStream {
        if(isOutputShutdown()) {
            throw SocketException("Socket output is shutdown")
        }

        return outputStream
    }

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
    @Throws(IOException::class)
    override suspend fun shutdownOutput() {
        if(!isOutputShutdown.getAndSet(true)) {
            @Suppress("BlockingMethodInNonBlockingContext")
            ch.shutdownOutput()
        }
    }
}