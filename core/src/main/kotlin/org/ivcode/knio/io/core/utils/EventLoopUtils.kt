package org.ivcode.org.ivcode.knio.io.core.utils

import org.ivcode.knio.core.EventLoop
import java.util.concurrent.CompletableFuture

fun EventLoop.startAsync(): CompletableFuture<Void> {
    return CompletableFuture.runAsync(this)
}