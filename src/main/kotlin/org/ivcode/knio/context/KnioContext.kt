package org.ivcode.knio.context

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.channels.Channel



private val DEFAULT_BYTE_BUFFER_POOL = ByteBufferPoolNone()
private val DEFAULT_CHANNEL_FACTORY = ChannelFactoryDefault()

private val DEFAULT_KNIO_CONTEXT = KnioContext()

/**
 * The coroutine context element for the knio library.
 *
 * @property taskBufferSize The size of buffers used for short-lived tasks. The memory used for these buffers depends on
 * the data type. For example, a [ByteBuffer] is 1:1, but a [CharBuffer] is 2:1.
 *
 * @property streamBufferSize The size of buffers used for streams. The memory used for these buffers depends on the
 * data type. For example, a [ByteBuffer] is 1:1, but a [CharBuffer] is 2:1.
 *
 * @property byteBufferPool The pool to use for acquiring [ByteBuffer] instances. This pool is used to create byte
 * buffers, but other buffers types are also pulled from this pool and a view is created.
 *
 * @property channelFactory The factory to use for creating [Channel] instances.
 */
data class KnioContext (
    val taskBufferSize: Int = DEFAULT_TASK_BUFFER_SIZE,
    val streamBufferSize: Int = DEFAULT_STREAM_BUFFER_SIZE,
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
