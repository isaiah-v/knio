package org.ivcode.knio.net


import org.ivcode.knio.lang.KAutoCloseable
import java.io.IOException
import java.net.InetAddress
import java.net.SocketAddress
import kotlin.jvm.Throws

interface KServerSocket: KAutoCloseable {

    @Throws(IOException::class)
    suspend fun accept(): KSocket

    @Throws(IOException::class)
    suspend fun bind(endpoint: SocketAddress, backlog: Int = 0)

    @Throws(IOException::class)
    override suspend fun close()

    @Throws(IOException::class)
    suspend fun getInetAddress(): InetAddress?

    @Throws(IOException::class)
    suspend fun getLocalPort(): Int

    @Throws(IOException::class)
    suspend fun getLocalSocketAddress(): SocketAddress?

    @Throws(IOException::class)
    suspend fun setAcceptTimeout(timeout: Long)

    @Throws(IOException::class)
    suspend fun getAcceptTimeout(): Long

    @Throws(IOException::class)
    suspend fun setReuseAddress(on: Boolean)

    @Throws(IOException::class)
    suspend fun getReuseAddress(): Boolean

    @Throws(IOException::class)
    suspend fun setReceiveBufferSize(size: Int)

    @Throws(IOException::class)
    suspend fun getReceiveBufferSize(): Int

    @Throws(IOException::class)
    suspend fun isBound(): Boolean

    @Throws(IOException::class)
    suspend fun isClosed(): Boolean
}