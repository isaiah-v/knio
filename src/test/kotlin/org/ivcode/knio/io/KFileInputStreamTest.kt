package org.ivcode.knio.io

import kotlinx.coroutines.runBlocking
import org.ivcode.org.ivcode.knio.io.copyTo
import org.ivcode.knio.lang.use
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayOutputStream
import java.io.FileInputStream

class KFileInputStreamTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "src/test/resources/test.txt",
    ])
    fun `compare java io to knio`(file: String) {

        // read file with java.io
        val expected = ByteArrayOutputStream().use {
            FileInputStream(file).use { fis ->
                fis.copyTo(it)
            }

            it.toByteArray()
        }

        // read file with knio
        val actual = runBlocking {
            ByteArrayOutputStream().use {
                KFileInputStream.open(file).use { fis ->
                    fis.copyTo(it)
                }

                it.toByteArray()
            }
        }

        assertTrue(expected.contentEquals(actual))
    }
}