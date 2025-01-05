package org.ivcode.org.ivcode.knio.io.core

import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey

/**
 * Interface representing a TCP channel.
 */
interface Channel {

    companion object {
        const val LISTEN_READ = SelectionKey.OP_READ
        const val LISTEN_WRITE = SelectionKey.OP_WRITE

        /**
         * Filters out any operations that are not supported.
         *
         * @param ops The operations to filter.
         * @return The filtered operations.
         */
        internal fun filterOps(ops: Int): Int {
            return ops and (LISTEN_READ or LISTEN_WRITE)
        }
    }

    /**
     * Gets the local address of the TCP channel.
     *
     * @return The local address of the TCP channel.
     */
    val localAddress: SocketAddress

    /**
     * Gets the remote address of the TCP channel.
     *
     * @return The remote address of the TCP channel.
     */
    val remoteAddress: SocketAddress

    /**
     * Writes data to the TCP channel.
     *
     * @param data The data to be written.
     */
    fun write(data: ByteBuffer)

    /**
     * Gets a reader for the TCP channel.
     *
     * @return The reader for the TCP channel.
     */
    fun reader(): Reader

    /**
     * Closes the TCP channel.
     */
    fun close()

    /**
     * Checks if the TCP channel is open.
     *
     * @return `true` if the TCP channel is open, `false` otherwise.
     */
    fun isOpen(): Boolean

    /**
     * Checks if the TCP channel is readable.
     *
     * @return `true` if the TCP channel is readable, `false` otherwise.
     */
    fun isReadable(): Boolean

    /**
     * Checks if the TCP channel is writable.
     *
     * @return `true` if the TCP channel is writable, `false` otherwise.
     */
    fun isWritable(): Boolean

    /**
     * Called to set the interest operations for the TCP channel.
     * Or the operations that the TCP channel is interested in.
     * A zero value indicates that the TCP channel is not interested in any operations.
     *
     * @param ops The interest operations to set.
     */
    fun setInterestOps(ops: Int)

    /**
     * Gets the current interest operations of the TCP channel.
     *
     * @return The current interest operations.
     */
    fun getInterestOps(): Int
}
