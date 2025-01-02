package org.ivcode.knio

import java.nio.channels.ClosedSelectorException
import java.nio.channels.SelectableChannel
import java.nio.channels.Selector
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * A class that represents an event loop for handling NIO events.
 */
class EventLoop: Runnable, AutoCloseable {

    private val selector = Selector.open()
    private val lock = ReentrantReadWriteLock()

    val size: Int get() = selector.keys().size
    val isOpen: Boolean get() = selector.isOpen

    /**
     * Registers a channel with the selector.
     *
     * @param channel The channel to register.
     * @param interestOps The interest operations for the channel.
     * @param eventHandler The event handler for the channel.
     * @throws ClosedSelectorException if the selector is closed.
     */
    fun register (
        channel: SelectableChannel,
        eventHandler: EventHandler,
        interestOps: Int = 0,
    ): Unit = readLock {
        if(!isOpen) {
            throw ClosedSelectorException()
        }

        channel.configureBlocking(false)
        val context = channel.register(selector, interestOps, eventHandler).let {
            val context = EventContext(eventHandler, EventKey(this, it))
            it.attach(context)
            context
        }
        eventHandler.onRegister(context.key)

        selector.wakeup()
    }

    /**
     * Runs the event loop.
     *
     * This method is blocking and will not return until the event loop is closed.
     */
    override fun run() {
        try {
            while (selector.isOpen) {
                select()
            }
        } catch (e: ClosedSelectorException) {
            // Do nothing
        } catch (e: ClosedSocketException) {
            // Do nothing
        }
    }

    /**
     * Selects the keys whose channels are ready for I/O operations.
     */
    private fun select() {
        selector.select()
        val keys = selector.selectedKeys()
        val iterator = keys.iterator()

        while (iterator.hasNext()) {
            val key = iterator.next()
            iterator.remove()

            (key.attachment() as EventContext).triggerEvent()
        }
    }

    /**
     * Runs the event associated with the given key attachment.
     *
     * @param context The key attachment containing the event handler.
     */
    private fun EventContext.triggerEvent() {
        try {
            onEvent()
        } catch (e: Exception) {
            this.close()
            e.printStackTrace()
        }
    }

    /**
     * Closes the event loop and all associated resources.
     */
    override fun close(): Unit = writeLock {
        if(!isOpen) {
            return@writeLock
        }

        // Iterate over all the keys registered with the selector
        selector.keys().forEach { key ->
            val att = key.attachment() as EventContext
            att.close() // Close the event handler and cancel the key
        }

        // Close the selector itself
        selector.close()
    }

    /**
     * Synchronizes on read lock.
     *
     * @param block The block of code to execute within the read lock.
     * @return The result of the block execution.
     */
    private fun <T> readLock (block: () -> T): T {
        lock.readLock().lock()
        try {
            return block()
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Synchronizes on write lock.
     *
     * @param block The block of code to execute within the write lock.
     * @return The result of the block execution.
     */
    private fun <T> writeLock (block: () -> T): T {
        lock.writeLock().lock()
        try {
            return block()
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * A data class that holds the event handler and the key.
     *
     * @property eventHandler The event handler associated with the key.
     * @property key The key associated with the event handler.
     */
    private data class EventContext (
        val eventHandler: EventHandler,
        val key: EventKey
    ) {
        /**
         * Handles the event.
         */
        fun onEvent() {
            eventHandler.onEvent()
        }

        /**
         * Closes the event handler and cancels the key.
         */
        fun close() {
            eventHandler.close()
            key.cancel()
        }
    }
}