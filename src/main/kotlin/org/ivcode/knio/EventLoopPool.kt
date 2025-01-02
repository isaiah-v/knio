package org.ivcode.knio

interface EventLoopPool {
    fun getEventLoop(): EventLoop
    fun close()
}