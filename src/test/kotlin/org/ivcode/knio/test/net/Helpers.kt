package org.ivcode.knio.test.net

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.lang.KAutoCloseable
import org.ivcode.knio.lang.use

fun runServer (server: AutoCloseable, block: suspend () -> Unit): Unit {
    server.use {
        runBlocking { block() }
    }
}

fun runServer (server: () -> KAutoCloseable, block: suspend () -> Unit): Unit {
    runBlocking { server().use { block() } }
}

fun <T> runServer (start: suspend ()-> T, close: suspend (T)->Unit, block: suspend () -> Unit): Unit {
    runBlocking {
        val server: T = start()
        try {
            block()
        } finally {
            close(server)
        }
    }
}