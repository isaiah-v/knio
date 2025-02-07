package org.knio.core.test.utils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

fun <T> runBlockingWithTimeout (timeoutMillis: Long, block: suspend () -> T): T = runBlocking {
    withTimeout(timeoutMillis) {
        block()
    }
}