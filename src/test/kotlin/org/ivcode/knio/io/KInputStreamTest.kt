package org.ivcode.knio.io

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.context.KnioContext
import org.ivcode.knio.context.getKnioContext
import org.ivcode.knio.lang.use
import org.ivcode.knio.utils.transferTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.test.assertEquals

class KInputStreamTest {
    private class SimpleByteArrayInputStream (
        data: ByteArray,
        context: KnioContext
    ) : KInputStream(context) {
        private var data: ByteBuffer = ByteBuffer.wrap(data)

        companion object {
            suspend fun open(data: ByteArray): SimpleByteArrayInputStream {
                return SimpleByteArrayInputStream(data, getKnioContext())
            }
        }

        override suspend fun read(b: ByteBuffer): Int {
            return data.transferTo(b)
        }
    }

    @Test
    fun `basic read byte test`() = runBlocking {
        val data: ByteArray = byteArrayOf(1, 2, 3, 4, 5)

        SimpleByteArrayInputStream.open(data).use { input ->
            assertEquals(1, input.read())
            assertEquals(2, input.read())
            assertEquals(3, input.read())
            assertEquals(4, input.read())
            assertEquals(5, input.read())
            assertEquals(-1, input.read())
        }
    }

    @Test
    fun `basic read array test`() = runBlocking {
        val data: ByteArray = byteArrayOf(1, 2, 3, 4, 5)

        SimpleByteArrayInputStream.open(data).use { input ->
            val buffer = ByteArray(5)
            assertEquals(5, input.read(buffer))
            assertEquals(true, data.contentEquals(buffer))
        }
    }

    @Test
    fun `skip test`() = runBlocking {
        val data: ByteArray = byteArrayOf(1, 2, 3, 4, 5)

        SimpleByteArrayInputStream.open(data).use { input ->
            assertEquals(1, input.read())
            assertEquals(2, input.read())
            input.skip(2)
            assertEquals(5, input.read())
            assertEquals(-1, input.read())
        }
    }

    @Test
    fun `mark not supported test`() = runBlocking {
        val data: ByteArray = byteArrayOf(1, 2, 3, 4, 5)

        SimpleByteArrayInputStream.open(data).use { input ->
            assertEquals(false, input.markSupported())
        }
    }

    @Test
    fun `mark reset test`(): Unit = runBlocking {
        val data: ByteArray = byteArrayOf(1, 2, 3, 4, 5)

        SimpleByteArrayInputStream.open(data).use { input ->
            assertEquals(false, input.markSupported())
            input.mark(3)
            assertEquals(1, input.read())

            assertThrows<IOException> {
                input.reset()
            }
        }
    }

    @Test
    fun `skip beyond EOF test`() = runBlocking {
        val data: ByteArray = byteArrayOf(1, 2, 3, 4, 5)

        SimpleByteArrayInputStream.open(data).use { input ->
            assertEquals(5, input.skip(10000))
        }
    }

    @Test
    fun `backward skip test`() = runBlocking {
        val data: ByteArray = byteArrayOf(1, 2, 3, 4, 5)
        SimpleByteArrayInputStream.open(data).use { input ->
            val buffer = ByteArray(5)
            input.read(buffer)

            // Backward skip is not supported in [KInputStream]
            assertEquals(0, input.skip(-5))
        }
    }

    @Test
    fun `test readAllBytes`() = runBlocking {
        val data: ByteArray = byteArrayOf(1, 2, 3, 4, 5)

        SimpleByteArrayInputStream.open(data).use { input ->
            val buffer = input.readAllBytes()
            assertEquals(true, data.contentEquals(buffer))
        }
    }

    @Test
    fun `test readNBytes`() = runBlocking {
        val data: ByteArray = byteArrayOf(1, 2, 3, 4, 5)

        SimpleByteArrayInputStream.open(data).use { input ->
            val buffer = input.readNBytes(3)
            assertEquals(byteArrayOf(1, 2, 3).contentEquals(buffer), true)
        }
    }
}