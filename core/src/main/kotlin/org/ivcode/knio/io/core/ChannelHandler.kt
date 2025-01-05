package org.ivcode.org.ivcode.knio.io.core

/**
 * Interface representing a handler for TCP channel events. This interface is used to interpret the data read from the
 * socket and to write data to the socket. If a higher level protocol is assumed, this interface should be implemented
 * to handle the protocol.
 */
interface ChannelHandler {

    /**
     * Called when the socket is connected. Each handler is associated with a single socket.
     *
     * @param channel The TCP channel that is connected.
     */
    fun onConnected(channel: Channel)

    /**
     * Called when data is available for reading.
     *
     * A read is triggered when data is available to be read from the socket. The data is not guaranteed to be a
     * complete message. This method will be called multiple times until no data is available from the socket.
     * The [onReadComplete] method is called when no more data is available.
     *
     * @param data The data read from the socket. The data is not guaranteed to be a complete message.
     */
    fun onRead()

    /**
     * Called when the socket is ready to write data.
     *
     * This method is called when the socket is ready to write data. The [Channel.write] method should be called
     */
    fun onWrite()

    /**
     * Called when the socket is closed.
     */
    fun onClosed()
}