package org.ivcode.knio.io

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.lang.use
import org.ivcode.knio.nio.knioInputStream
import org.ivcode.org.ivcode.knio.io.readText
import org.ivcode.org.ivcode.knio.io.reader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.io.path.Path

class KInputStreamReaderTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "src/test/resources/test.txt",
    ])
    fun `InputStreamReader - java vs knio`(file: String) {
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
}