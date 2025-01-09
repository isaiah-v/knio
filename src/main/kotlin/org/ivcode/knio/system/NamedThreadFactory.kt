package org.ivcode.org.ivcode.knio.system

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

class NamedThreadFactory(
    private val name: String,
    private val virtual: Boolean = true
): ThreadFactory {
    private val counter = AtomicLong(1)

    override fun newThread(r: Runnable): Thread {
        val thread = if(virtual) {
            Thread.ofVirtual().unstarted(r)
        } else {
            Thread(r)
        }
        thread.name = "$name-${counter.getAndIncrement()}"

        return thread
    }
}