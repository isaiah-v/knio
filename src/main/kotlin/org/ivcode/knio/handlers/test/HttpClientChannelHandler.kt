package org.ivcode.knio.handlers.test

import org.ivcode.knio.Channel
import org.ivcode.knio.ChannelHandler
import org.ivcode.knio.Channel.Companion.LISTEN_READ
import org.ivcode.knio.Channel.Companion.LISTEN_WRITE
import java.nio.ByteBuffer

class HttpClientChannelHandler: ChannelHandler {

    private lateinit var channel: Channel

    override fun onConnected(channel: Channel) {
        println("Socket Opened: ${channel.remoteAddress}")
        this.channel = channel

        // Prepare the channel to write data.
        this.channel.setInterestOps(LISTEN_WRITE);
    }

    override fun onRead() {
        println("Socket Read Progress: ${channel.remoteAddress}")

        val reader = channel.reader()

        var count = 0
        while(reader.read()) {
            if(!reader.buffer.hasRemaining()) {
                break
            }

            val remaining = reader.buffer.remaining()
            count += remaining
            val bytes = ByteArray(reader.buffer.remaining())
            reader.buffer.get(bytes)
            val request = String(bytes)
            print(request)
        }

        if(count > 0) {
            println("\nSocket Read Complete: ${channel.remoteAddress}")
            channel.close()
        }
    }

    override fun onWrite() {
        val request = """
        GET / HTTP/1.1
        Host: localhost
        Accept: application/json, text/plain, */*
        User-Agent: bruno-runtime/1.36.1
        Connection: keep-alive
        Content-Length: 0
        
        
        """.trimIndent()

        val writeBuffer = ByteBuffer.wrap(request.toByteArray())
        while (writeBuffer.hasRemaining()) {
            channel.write(writeBuffer)
        }

        channel.setInterestOps(LISTEN_READ)
    }

    override fun onClosed() {
        println("Socket Closed: ${channel.remoteAddress}")
    }
}