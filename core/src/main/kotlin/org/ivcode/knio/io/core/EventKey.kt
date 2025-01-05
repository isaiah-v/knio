package org.ivcode.org.ivcode.knio.io.core

import java.nio.channels.CancelledKeyException
import java.nio.channels.SelectionKey

/**
 * Represents a key for a NIO event.
 *
 * @property eventLoop The event loop associated with this key.
 * @property key The underlying selection key.
 */
data class EventKey (
    val eventLoop: EventLoop,
    private val key: SelectionKey,
) {
    /**
     * This wrapper is used to hide the selector from the [EventHandler] class.
     * For the close method to work, we can't re-register the channel with the selector.
     */

    /** The channel associated with this key. */
    val channel get() = key.channel()!!

    /**
     * Indicates if the key's channel is ready for reading.
     * @throws CancelledKeyException if the key is cancelled.
     */
    val isReadable: Boolean get() = key.isReadable

    /**
     * Indicates if the key's channel is ready for writing.
     * @throws CancelledKeyException if the key is cancelled.
     */
    val isWritable: Boolean get() = key.isWritable

    /**
     * Indicates if the key's channel is ready to connect.
     * @throws CancelledKeyException if the key is cancelled.
     */
    val isConnectable: Boolean get() = key.isConnectable

    /**
     * Indicates if the key's channel is ready to accept connections.
     * @throws CancelledKeyException if the key is cancelled.
     */
    val isAcceptable: Boolean get() = key.isAcceptable

    /** Indicates if the key is valid. */
    val isValid: Boolean get() = key.isValid

    /**
     * The interest operations for this key.
     * Setting this value will also wake up the selector.
     */
    var interestOps: Int
        get() = key.interestOps()
        set(value) {
            key.interestOps(value)
            key.selector().wakeup()
        }

    /**
     * Cancels the key.
     */
    fun cancel() {
        key.cancel()
    }
}