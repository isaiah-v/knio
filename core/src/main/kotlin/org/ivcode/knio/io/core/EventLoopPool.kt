package org.ivcode.org.ivcode.knio.io.core

interface EventLoopPool {
    fun getEventLoop(): EventLoop
    fun close()
}