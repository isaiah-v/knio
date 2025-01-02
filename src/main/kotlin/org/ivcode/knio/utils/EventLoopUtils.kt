package org.ivcode.knio.utils

import org.ivcode.knio.EventLoop
import java.util.concurrent.CompletableFuture

fun EventLoop.startAsync(): CompletableFuture<Void> {
    return CompletableFuture.runAsync(this)
}