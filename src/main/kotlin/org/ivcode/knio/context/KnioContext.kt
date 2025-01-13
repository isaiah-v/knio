package org.ivcode.knio.context

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private val DEFAULT_BYTE_BUFFER_POOL = ByteBufferNoPool()
private val DEFAULT_CHANNEL_FACTORY = ChannelFactoryDefault()

private val DEFAULT_KNIO_CONTEXT = KnioContext(
    byteBufferPool = DEFAULT_BYTE_BUFFER_POOL,
    channelFactory = DEFAULT_CHANNEL_FACTORY
)

data class KnioContext (
    val byteBufferPool: ByteBufferPool,
    val channelFactory: ChannelFactory
): CoroutineContext.Element {

    companion object Key: CoroutineContext.Key<KnioContext>

    override val key: CoroutineContext.Key<KnioContext>
        get() = Key
}

internal suspend fun knioContext(): KnioContext {
    return coroutineContext[KnioContext] ?: DEFAULT_KNIO_CONTEXT
}
