package org.ivcode.knio.io

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.lang.knioReader
import org.ivcode.knio.lang.use
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KBufferedReaderTest {

    @Test
    fun `read char test`() = runBlocking {

        "Hello World".knioReader().buffered().use { reader ->
            val ch = reader.read()
            assertEquals('H', ch.toChar())
        }

    }

    @Test
    fun `read to array`() = runBlocking {

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
        "Hello\nWorld".knioReader().buffered().use { reader ->
            val line = reader.readLine()
            assertEquals("Hello", line)

            val line2 = reader.readLine()
            assertEquals("World", line2)

            val line3 = reader.readLine()
            assertEquals(null, line3)
        }
    }

}