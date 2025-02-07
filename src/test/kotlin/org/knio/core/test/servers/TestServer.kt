package org.knio.core.test.servers

import kotlinx.coroutines.runBlocking

interface TestServer {
    suspend fun start(): TestServer
    suspend fun stop()
}
