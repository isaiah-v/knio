package org.knio.core.io

import kotlinx.coroutines.runBlocking
import org.knio.core.lang.knioReader
import org.knio.core.lang.use
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.nio.CharBuffer

class KBufferedReaderTest {

    @Test
    fun `test mark supported`() = runBlocking {
        // mark is always supported in a buffered reader
        val expected = true
        val actual = "Hello World".knioReader().buffered().markSupported()

        assertEquals(expected, actual)
    }

    @Test
    fun `read char test`() = runBlocking {

        // Java
        "Hello World".reader().buffered().use { reader ->
            val ch = reader.read()
            assertEquals('H', ch.toChar())
        }

        // Knio
        "Hello World".knioReader().buffered().use { reader ->
            val ch = reader.read()
            assertEquals('H', ch.toChar())
        }

    }

    @Test
    fun `read to array`() = runBlocking {

        // Java
        "Hello World".reader().buffered().use { reader ->
            val buffer = CharArray(5)
            var r = reader.read(buffer)

            assertEquals(5, r)
            assertEquals("Hello", String(buffer, 0, r))

            r = reader.read(buffer)
            assertEquals(5, r)
            assertEquals(" Worl", String(buffer, 0, r))

            r = reader.read(buffer)
            assertEquals(1, r)
            assertEquals("d", String(buffer, 0, r))
        }

        // Knio
        "Hello World".knioReader().buffered().use { reader ->
            val buffer = CharArray(5)
            var r = reader.read(buffer)

            assertEquals(5, r)
            assertEquals("Hello", String(buffer, 0, r))

            r = reader.read(buffer)
            assertEquals(5, r)
            assertEquals(" Worl", String(buffer, 0, r))

            r = reader.read(buffer)
            assertEquals(1, r)
            assertEquals("d", String(buffer, 0, r))
        }
    }

    @Test
    fun `read line`() = runBlocking {
        // Java
        "Hello\nWorld".reader().buffered().use { reader ->
            val line = reader.readLine()
            assertEquals("Hello", line)

            val line2 = reader.readLine()
            assertEquals("World", line2)

            val line3 = reader.readLine()
            assertEquals(null, line3)
        }

        // Knio
        "Hello\nWorld".knioReader().buffered().use { reader ->
            val line = reader.readLine()
            assertEquals("Hello", line)

            val line2 = reader.readLine()
            assertEquals("World", line2)

            val line3 = reader.readLine()
            assertEquals(null, line3)
        }
    }

    @Test
    fun `read line with carriage return`() = runBlocking {
        // Java
        "Hello\r\nWorld".reader().buffered().use { reader ->
            val line = reader.readLine()
            assertEquals("Hello", line)

            val line2 = reader.readLine()
            assertEquals("World", line2)

            val line3 = reader.readLine()
            assertEquals(null, line3)
        }

        // Knio
        "Hello\r\nWorld".knioReader().buffered().use { reader ->
            val line = reader.readLine()
            assertEquals("Hello", line)

            val line2 = reader.readLine()
            assertEquals("World", line2)

            val line3 = reader.readLine()
            assertEquals(null, line3)
        }
    }

    @Test
    fun `read line with carriage return and no line feed`() = runBlocking {
        // Java
        "Hello\rWorld".reader().buffered().use { reader ->
            val line = reader.readLine()
            assertEquals("Hello", line)

            val line2 = reader.readLine()
            assertEquals("World", line2)

            val line3 = reader.readLine()
            assertEquals(null, line3)
        }

        // Knio
        "Hello\rWorld".knioReader().buffered().use { reader ->
            val line = reader.readLine()
            assertEquals("Hello", line)

            val line2 = reader.readLine()
            assertEquals("World", line2)

            val line3 = reader.readLine()
            assertEquals(null, line3)
        }
    }

    @Test
    fun `read line with no line feed`() = runBlocking {
        // Java
        "Hello World".reader().buffered().use { reader ->
            val line = reader.readLine()
            assertEquals("Hello World", line)

            val line2 = reader.readLine()
            assertEquals(null, line2)
        }

        // Knio
        "Hello World".knioReader().buffered().use { reader ->
            val line = reader.readLine()
            assertEquals("Hello World", line)

            val line2 = reader.readLine()
            assertEquals(null, line2)
        }
    }

    @Test
    fun `mark with negative readAheadLimit`(): Unit = runBlocking {
        // Java
        assertThrows<IllegalArgumentException> {
            "Hello World".reader().buffered().use { reader ->
                reader.mark(-1)
            }
        }

        // Knio
        assertThrows<IllegalArgumentException> {
            "Hello World".knioReader().buffered().use { reader ->
                reader.mark(-1)
            }
        }
    }

    @Test
    fun `basic mark reset`() = runBlocking {
        "Hello World".reader().buffered().use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.mark(5)
            assertEquals('e', reader.read().toChar())
            assertEquals('l', reader.read().toChar())
            assertEquals('l', reader.read().toChar())
            assertEquals('o', reader.read().toChar())
            reader.reset()
            assertEquals('e', reader.read().toChar())
        }

        "Hello World".knioReader().buffered().use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.mark(5)
            assertEquals('e', reader.read().toChar())
            assertEquals('l', reader.read().toChar())
            assertEquals('l', reader.read().toChar())
            assertEquals('o', reader.read().toChar())
            reader.reset()
            assertEquals('e', reader.read().toChar())
        }
    }

    @Test
    fun `mark reset with resize`() = runBlocking {
        "Hello World".reader().buffered(3).use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.mark(5)
            assertEquals('e', reader.read().toChar())
            assertEquals('l', reader.read().toChar())
            assertEquals('l', reader.read().toChar())
            assertEquals('o', reader.read().toChar())
            reader.reset()
            assertEquals('e', reader.read().toChar())
        }

        "Hello World".knioReader().buffered(3).use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.mark(5)
            assertEquals('e', reader.read().toChar())
            assertEquals('l', reader.read().toChar())
            assertEquals('l', reader.read().toChar())
            assertEquals('o', reader.read().toChar())
            reader.reset()
            assertEquals('e', reader.read().toChar())
        }
    }

    @Test
    fun `invalidate mark`(): Unit = runBlocking {
        // Documentation states an exception is thrown if the buffer is read past the mark limit, but it not explicitly
        // true. The following conditions must be met for an exception to be thrown:
        //  1) A read operation to populate the buffer must be called
        //  2) At the time of calling the read operation, it must be passed the readAheadLimit

        assertThrows<IOException> {
            "Hello World".reader().buffered(3).use { reader ->
                assertEquals('H', reader.read().toChar())
                reader.mark(3)
                assertEquals('e', reader.read().toChar())
                assertEquals('l', reader.read().toChar())
                assertEquals('l', reader.read().toChar())
                assertEquals('o', reader.read().toChar())
                assertEquals(' ', reader.read().toChar())
                assertEquals('W', reader.read().toChar())
                reader.reset()
            }
        }

        assertThrows<IOException> {
            "Hello World".knioReader().buffered(3).use { reader ->
                assertEquals('H', reader.read().toChar())
                reader.mark(3)
                assertEquals('e', reader.read().toChar())
                assertEquals('l', reader.read().toChar())
                assertEquals('l', reader.read().toChar())
                assertEquals('o', reader.read().toChar())
                assertEquals(' ', reader.read().toChar())
                assertEquals('W', reader.read().toChar())
                reader.reset()
            }
        }
    }

    @Test
    fun `test read into CharBuffer`() = runBlocking {

        // Java
        "Hello World".reader().buffered().use { reader ->
            val buffer = CharBuffer.allocate(5)

            var r = reader.read(buffer)
            assertEquals(5, r)
            assertEquals("Hello", buffer.flip().toString())

            r = reader.read(buffer)
            assertEquals(5, r)
            assertEquals(" Worl", buffer.flip().toString())

            r = reader.read(buffer)
            assertEquals(1, r)
            assertEquals("d", buffer.flip().toString())
        }

        // Knio
        "Hello World".knioReader().buffered().use { reader ->
            val buffer = CharBuffer.allocate(5)

            var r = reader.read(buffer)
            assertEquals(5, r)
            assertEquals("Hello", buffer.flip().toString())

            r = reader.read(buffer)
            assertEquals(5, r)
            assertEquals(" Worl", buffer.flip().toString())

            r = reader.read(buffer)
            assertEquals(1, r)
            assertEquals("d", buffer.flip().toString())
        }
    }

    @Test
    fun `test read into CharBuffer with smaller internal buffer`() = runBlocking {

        // Java
        "Hello World".reader().buffered(3).use { reader ->
            val buffer = CharBuffer.allocate(5)

            var r = reader.read(buffer)
            assertEquals(5, r)
            assertEquals("Hello", buffer.flip().toString())

            r = reader.read(buffer)
            assertEquals(5, r)
            assertEquals(" Worl", buffer.flip().toString())

            r = reader.read(buffer)
            assertEquals(1, r)
            assertEquals("d", buffer.flip().toString())
        }

        // Knio
        "Hello World".knioReader().buffered(3).use { reader ->
            val buffer = CharBuffer.allocate(5)

            var r = reader.read(buffer)
            assertEquals(5, r)
            assertEquals("Hello", buffer.flip().toString())

            r = reader.read(buffer)
            assertEquals(5, r)
            assertEquals(" Worl", buffer.flip().toString())

            r = reader.read(buffer)
            assertEquals(1, r)
            assertEquals("d", buffer.flip().toString())
        }
    }

    @Test
    fun `basic skip`() = runBlocking {
        // Java
        "Hello World".reader().buffered().use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.skip(5)
            assertEquals('W', reader.read().toChar())
        }

        // Knio
        "Hello World".knioReader().buffered().use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.skip(5)
            assertEquals('W', reader.read().toChar())
        }
    }

    @Test
    fun `skip with small buffer`() = runBlocking {
        // Test skipping more than the buffer size

        // Java
        "Hello World".reader().buffered(3).use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.skip(5)
            assertEquals('W', reader.read().toChar())
        }

        // Knio
        "Hello World".knioReader().buffered(3).use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.skip(5)
            assertEquals('W', reader.read().toChar())
        }
    }

    @Test
    fun `mark skip reset`() = runBlocking {
        // Java
        "Hello World".reader().buffered().use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.mark(6)
            reader.skip(5)
            assertEquals('W', reader.read().toChar())
            reader.reset()
            assertEquals('e', reader.read().toChar())
        }

        // Knio
        "Hello World".knioReader().buffered().use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.mark(6)
            reader.skip(5)
            assertEquals('W', reader.read().toChar())
            reader.reset()
            assertEquals('e', reader.read().toChar())
        }
    }

    @Test
    fun `mark skip reset with a small buffer`() = runBlocking {
        // Java

        "Hello World".reader().buffered(2).use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.mark(5)
            reader.skip(4)
            assertEquals(' ', reader.read().toChar())
            reader.reset()
            assertEquals('e', reader.read().toChar())
        }


        // Knio
        "Hello World".knioReader().buffered(2).use { reader ->
            assertEquals('H', reader.read().toChar())
            reader.mark(5)
            reader.skip(4)
            assertEquals(' ', reader.read().toChar())
            reader.reset()
            assertEquals('e', reader.read().toChar())
        }
    }

    @Test
    fun `read after close`(): Unit = runBlocking {
        // Java
        "Hello World".reader().buffered().use { reader ->
            reader.close()
            assertThrows<IOException> {
                reader.read()
            }
        }

        // Knio
        "Hello World".knioReader().buffered().use { reader ->
            reader.close()
            assertThrows<IOException> {
                reader.read()
            }
        }
    }

}