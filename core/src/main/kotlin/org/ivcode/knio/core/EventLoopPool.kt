package org.ivcode.knio.core

interface EventLoopPool {
    fun getEventLoop(): EventLoop
    fun close()
}