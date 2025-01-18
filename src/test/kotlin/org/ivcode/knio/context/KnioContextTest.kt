package org.ivcode.knio.context

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KnioContextTest {

    @Test
    fun `use heap ByteBuffer`() = runBlocking {
        val pool = ByteBufferPoolNone(isDirect = false)
        val context = KnioContext(byteBufferPool = pool)

        withContext(context) {
            val buffer = getKnioContext().byteBufferPool.acquire(1024)
            assert(!buffer.isDirect)
        }
    }

    @Test
    fun `use direct ByteBuffer`() = runBlocking {
        val pool = ByteBufferPoolNone(isDirect = true)
        val context = KnioContext(byteBufferPool = pool)

        withContext(context) {
            val buffer = getKnioContext().byteBufferPool.acquire(1024)
            assert(buffer.isDirect)
        }
    }

    @Test
    fun `default context is the same instance`(): Unit = runBlocking {
        val knioContext1 = getKnioContext()
        val knioContext2 = getKnioContext()

        assertTrue(knioContext1 === knioContext2)
    }

    @Test
    fun `context is not the same in a withContext block`(): Unit = runBlocking {
        // Base context
        val knioContext1 = getKnioContext()

        withContext(KnioContext()) {
            // Context inside withContext block
            val knioContext2 = getKnioContext()
            assertTrue(knioContext1 !== knioContext2)
        }

        // Context outside withContext block
        val knioContext3 = getKnioContext()

        assertTrue(knioContext1 === knioContext3)
    }
}