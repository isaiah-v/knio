package org.ivcode.knio.test.net

import kotlinx.coroutines.runBlocking

fun runServer (server: AutoCloseable, block: suspend () -> Unit): Unit {
    server.use {
        runBlocking { block() }
    }
}