package org.ivcode.knio.context

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private val DEFAULT_BYTE_BUFFER_POOL = ByteBufferNoPool()
private val DEFAULT_CHANNEL_FACTORY = ChannelFactoryDefault()

private val DEFAULT_KNIO_CONTEXT = KnioContext()

data class KnioContext (
    val byteBufferPool: ByteBufferPool = DEFAULT_BYTE_BUFFER_POOL,
    val channelFactory: ChannelFactory = DEFAULT_CHANNEL_FACTORY
): CoroutineContext.Element {

    companion object Key: CoroutineContext.Key<KnioContext>

    override val key: CoroutineContext.Key<KnioContext>
        get() = Key
}

internal suspend fun getKnioContext(): KnioContext {
    return coroutineContext[KnioContext] ?: DEFAULT_KNIO_CONTEXT
}
