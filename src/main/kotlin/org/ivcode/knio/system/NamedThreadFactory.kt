package org.ivcode.org.ivcode.knio.system

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

class NamedVirtualThreadFactory(
    private val name: String
): ThreadFactory {

    private val counter = AtomicLong(1)

    override fun newThread(r: Runnable): Thread {
        val thread = Thread.ofVirtual().unstarted(r)
        thread.name = "$name-${counter.getAndIncrement()}"

        return thread
    }
}