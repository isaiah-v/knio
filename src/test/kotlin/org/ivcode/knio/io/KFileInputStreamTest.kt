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
    fun `FileInputStream - java vs knio`(file: String) {

        // read file with java.io
        val expectedExec = {
            ByteArrayOutputStream().use { outputStream ->
                FileInputStream(file).use { fis ->
                    fis.copyTo(outputStream)
                }

                outputStream.toByteArray()
            }
        }

        val actualExec = suspend {
            ByteArrayOutputStream().use { outputStream ->
                KFileInputStream.open(file).use { fis ->
                    fis.copyTo(outputStream)
                }

                outputStream.toByteArray()
            }
        }

        val expected = expectedExec()
        val actual = runBlocking { actualExec() }

        assertTrue(expected.contentEquals(actual))
    }
}