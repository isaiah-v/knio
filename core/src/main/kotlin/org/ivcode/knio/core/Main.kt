package org.ivcode.knio.core

import org.ivcode.knio.core.handlers.test.HttpServerChannelHandler
import org.ivcode.knio.core.tcp.utils.registerTcpServerSSL
import org.ivcode.knio.core.utils.createSSLContext

fun main(args: Array<String>) {

    val sslContext = createSSLContext()

    val eventLoop = EventLoop()


    eventLoop.registerTcpServerSSL(443, sslContext) { HttpServerChannelHandler () }
    eventLoop.run()
}