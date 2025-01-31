package org.ivcode.knio.test.servers.reverse

interface ReverseServer {
    suspend fun start(): ReverseServer
    suspend fun stop()
}