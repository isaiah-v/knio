package org.ivcode.knio.context

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

internal class ThreadFactoryNamed (
    private val name: String,
    private val daemon: Boolean = true,
    private val priority: Int = Thread.NORM_PRIORITY,
): ThreadFactory {
    private val counter = AtomicLong(1)

    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r, "$name-${counter.getAndIncrement()}")
        thread.isDaemon = daemon
        thread.priority = priority

        return thread
    }
}