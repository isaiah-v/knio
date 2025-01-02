package org.ivcode.knio

/**
 * Interface representing a handler for NIO events.
 */
interface EventHandler: AutoCloseable {

    /**
     * Called when the key is registered with the selector.
     *
     * @param key The key that was registered.
     */
    fun onRegister(key: EventKey)

    /**
     * Called when an event occurs.
     */
    fun onEvent()

    /**
     * Closes this handler and releases any system resources associated with it.
     */
    override fun close()
}