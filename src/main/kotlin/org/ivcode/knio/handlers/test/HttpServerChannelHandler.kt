package org.ivcode.knio.handlers.test

import org.ivcode.knio.ChannelHandler
import org.ivcode.knio.Channel
import org.ivcode.knio.Channel.Companion.LISTEN_READ
import org.ivcode.knio.Channel.Companion.LISTEN_WRITE
import org.ivcode.knio.html1.HtmlRequestHead
import org.ivcode.knio.html1.HtmlRequestHeadReader
import java.nio.ByteBuffer

/**
 * A test handler for HTTP OK responses over a TCP channel.
 */
class HttpServerChannelHandler: ChannelHandler {

    private var headReader = HtmlRequestHeadReader()

    /**
     * The TCP channel associated with this handler.
     */
    private lateinit var channel: Channel
    private lateinit var head: HtmlRequestHead

    /**
     * Called when the channel is connected.
     *
     * @param channel The TCP channel that is connected.
     */
    override fun onConnected(channel: Channel) {
        println("Socket Opened: ${channel.remoteAddress}")
        this.channel = channel

        // Set the channel as ready to read data.
        this.channel.setInterestOps(LISTEN_READ)
    }

    /**
     * Called when data is read from the channel.
     *
     * @param data The data read from the channel.
     */
    override fun onRead() {
        if (!headReader.isDone() && headReader.doRead(this.channel)) {
            head = headReader.build()
        }

        val reader = channel.reader()
        var count = 0
        do {
            while (reader.buffer.hasRemaining()) {
                val remaining = reader.buffer.remaining()
                count += remaining
                val bytes = ByteArray(reader.buffer.remaining())
                reader.buffer.get(bytes)
                val request = String(bytes)
                print(request)
            }
            reader.buffer.clear()
        } while (reader.read())

        if(count > 0) {
            channel.setInterestOps(LISTEN_WRITE)
        }
    }

    /**
     * Called when the channel is ready to write data.
     */
    override fun onWrite() {
        val response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n"

        val writeBuffer = ByteBuffer.wrap(response.toByteArray())
        while (writeBuffer.hasRemaining()) {
            channel.write(writeBuffer)
        }

        // Close the channel after writing the response.
        channel.close()
    }

    /**
     * Called when the channel is closed.
     */
    override fun onClosed() {
        println("Socket Closed: ${channel.remoteAddress}")
    }
}