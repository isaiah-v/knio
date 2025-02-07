package org.knio.core.io

import kotlinx.coroutines.runBlocking
import org.knio.core.lang.knioInputStream
import org.knio.core.lang.use
import org.knio.core.nio.knioInputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.io.path.Path

class KInputStreamReaderTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "src/test/resources/test.txt",
    ])
    fun `java vs knio`(file: String) {
        val expectedExec = {
            File(file).inputStream().reader().use {
                it.readText()
            }
        }

        val actualExec = suspend {
            Path(file).knioInputStream().reader().use {
                it.readText()
            }
        }

        val expected = expectedExec()
        val actual = runBlocking { actualExec() }

        assertEquals(expected, actual)
    }

    @Test
    fun `test encoding`() = runBlocking {
        val string = "this is a test string"
        val data = string.toByteArray(Charsets.UTF_32BE)
        val dataUtf8 = string.toByteArray(Charsets.UTF_8)
        val encoding = Charsets.UTF_32BE.name()

        // UTF-8 is default, test data can't be the same as UTF-8
        assertEquals(false, data.contentEquals(dataUtf8))

        // java
        val actualJava = data.inputStream().reader(Charsets.UTF_32BE).use {
            assertEquals(encoding, it.encoding)
            it.readText()
        }
        assertEquals(string, actualJava)

        // knio
        val actualKnio = data.knioInputStream().reader(Charsets.UTF_32BE).use {
            assertEquals(encoding, it.encoding)
            it.readText()
        }
        assertEquals(string, actualKnio)
    }
}