package org.ivcode.knio.system

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

class ThreadFactorySystem (
    private val name: String,
    private val daemon: Boolean,
    private val priority: Int
): ThreadFactory {
    private val counter = AtomicLong(1)

    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r, "$name-${counter.getAndIncrement()}")
        thread.isDaemon = daemon
        thread.priority = priority

        return thread
    }
}