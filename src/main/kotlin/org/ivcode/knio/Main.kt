package org.ivcode.knio

import org.ivcode.knio.handlers.test.HttpServerChannelHandler
import org.ivcode.knio.tcp.utils.registerTcpServerSSL
import org.ivcode.knio.utils.createSSLContext

fun main(args: Array<String>) {

    val sslContext = createSSLContext()

    val eventLoop = EventLoop()


    eventLoop.registerTcpServerSSL(443, sslContext) { HttpServerChannelHandler () }
    eventLoop.run()
}