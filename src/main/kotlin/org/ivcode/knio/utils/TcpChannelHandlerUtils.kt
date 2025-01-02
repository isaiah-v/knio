package org.ivcode.knio.utils

import org.ivcode.knio.Channel
import org.ivcode.knio.ChannelHandler


/**
 * A class that intercepts the methods of a TcpChannelHandler and allows for additional callbacks to be added.
 */
private class InterceptedHandler(
    private val chain: ChannelHandler,
    private val onConnected: ChannelHandler.(channel: Channel) -> Unit = ChannelHandler::onConnected,
    private val onRead: ChannelHandler.() -> Unit = ChannelHandler::onRead,
    private val onWrite: ChannelHandler.() -> Unit = ChannelHandler::onWrite,
    private val onClosed: ChannelHandler.() -> Unit = ChannelHandler::onClosed,
): ChannelHandler {
    override fun onConnected(channel: Channel) {
        onConnected.invoke(chain, channel)
    }

    override fun onRead() {
        onRead.invoke(chain)
    }

    override fun onWrite() {
        onWrite.invoke(chain)
    }

    override fun onClosed() {
        onClosed.invoke(chain)
    }

}

/**
 * Adds a callback to the onConnected method.
 *
 * @param onConnected The callback to be called when the onConnected method is called.
 * @return A new instance of TcpChannelHandler with the callback added.
 */
fun ChannelHandler.onClose (
    close: ()-> Unit = {}
): ChannelHandler {
    return InterceptedHandler(this, onClosed = {
        onClosed()
        close.invoke()
    })
}

fun Channel.addWriteInterest() {
    val ops = this.getInterestOps()
    this.setInterestOps(ops or Channel.LISTEN_WRITE)
}

fun Channel.removeWriteInterest() {
    val ops = this.getInterestOps()
    this.setInterestOps(ops and Channel.LISTEN_WRITE.inv())
}