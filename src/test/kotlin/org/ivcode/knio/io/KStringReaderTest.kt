package org.ivcode.knio.io

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.lang.knioReader
import org.ivcode.knio.lang.use
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KStringReaderTest {

    @Test
    fun `basic test`() = runBlocking {
        // java
        "Hello World".reader().use {
            val text = it.readText()
            assert(text == "Hello World")
        }

        // knio
        "Hello World".knioReader().use {
            val text = it.readText()
            assert(text == "Hello World")
        }
    }

    @Test
    fun `test skip forward`() = runBlocking {
        // java
        "Hello World".reader().use {
            it.skip(6)
            val text = it.readText()
            assert(text == "World")
        }

        // knio
        "Hello World".knioReader().use {
            it.skip(6)
            val text = it.readText()
            assert(text == "World")
        }
    }

    @Test
    fun `test skip backward`() = runBlocking {
        // java
        "Hello World".reader().use {
            it.skip(6)
            it.skip(-5)
            val text = it.readText()
            assert(text == "ello World")
        }

        // knio
        "Hello World".knioReader().use {
            it.skip(6)
            it.skip(-5)
            val text = it.readText()
            assert(text == "ello World")
        }
    }

    @Test
    fun `always ready`() = runBlocking {
        // java
        "Hello World".reader().use {
            assert(it.ready())
        }

        // knio
        "Hello World".knioReader().use {
            assert(it.ready())
        }
    }

    @Test
    fun `mark supported`() = runBlocking {
        // java
        "Hello World".reader().use {
            assert(it.markSupported())
        }

        // knio
        "Hello World".knioReader().use {
            assert(it.markSupported())
        }
    }

    @Test
    fun `mark reset`() = runBlocking {
        // java
        "Hello World".reader().use {
            it.mark(6)
            it.skip(5)
            it.reset()
            val text = it.readText()
            assertEquals("Hello World", text)
        }

        // knio
        "Hello World".knioReader().use {
            it.mark(6)
            it.skip(5)
            it.reset()
            val text = it.readText()
            assertEquals("Hello World", text)
        }
    }

    @Test
    fun `mark reset beyond mark`(): Unit = runBlocking {
        // java
        "Hello World".reader().use {
            it.mark(2)
            it.read()
            it.read()
            it.read()

            // reset beyond mark, no exception
            it.reset()
        }

        // knio
        "Hello World".knioReader().use {
            it.mark(2)
            it.read()
            it.read()
            it.read()

            // reset beyond mark, no exception
            it.reset()
        }
    }

    @Test
    fun `mark reset without mark`(): Unit = runBlocking {
        // java
        "Hello World".reader().use {
            it.skip(6)

            // no exception, just returns 0
            it.reset()

            // read is at 0
            assertEquals('H', it.read().toChar())
        }

        // knio
        "Hello World".knioReader().use {
            it.skip(6)

            // no exception, just returns 0
            it.reset()

            // read is at 0
            assertEquals('H', it.read().toChar())
        }
    }
}