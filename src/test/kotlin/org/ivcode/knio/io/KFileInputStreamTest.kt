package org.ivcode.knio.io

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.lang.use
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.test.assertEquals


class KFileInputStreamTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "src/test/resources/test.txt",
    ])
    fun `java vs knio`(file: String) {

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

    @Test
    fun `mark and reset work correctly`() = runBlocking {
        val file = "src/test/resources/test.txt"

        // Java
        // N/A mark and reset not supported


        // Knio
        KFileInputStream.open(file).use { input ->
            input.mark(10)
            val buffer = ByteBuffer.allocate(5)
            input.read(buffer)
            input.reset()
            val resetBuffer = ByteBuffer.allocate(5)
            input.read(resetBuffer)
            assertTrue(buffer.array().contentEquals(resetBuffer.array()))
        }

    }

    @Test
    fun `skip skips the correct number of bytes`() = runBlocking {
        val file = "src/test/resources/test.txt"

        // Java
        FileInputStream(file).use { input ->
            val skippedBytes = input.skip(5)
            assertTrue(skippedBytes == 5L)
        }

        // Knio
        KFileInputStream.open(file).use { input ->
            val skippedBytes = input.skip(5)
            assertTrue(skippedBytes == 5L)
        }
    }

    @Test
    fun `read reads the correct number of bytes`() = runBlocking {
        val file = "src/test/resources/test.txt"
        // Java
        FileInputStream(file).use { input ->
            val buffer = ByteArray(5)
            val bytesRead = input.read(buffer)
            assertTrue(bytesRead == 5)
        }

        // Knio
        KFileInputStream.open(file).use { input ->
            val buffer = ByteBuffer.allocate(5)
            val bytesRead = input.read(buffer)
            assertTrue(bytesRead == 5)
        }
    }

    @Test
    fun `close closes the file input stream`(): Unit = runBlocking {
        val file = "src/test/resources/test.txt"

        // Java
        val input = FileInputStream(file)
        input.close()
        assertThrows<IOException> {
            runBlocking { input.read() }
        }

        // Knio
        val kInput = KFileInputStream.open(file)
        kInput.close()
        assertThrows<IOException> {
            runBlocking { kInput.read() }
        }
    }

    @Test
    fun `test is mark supported`() = runBlocking {
        val file = "src/test/resources/test.txt"

        // Java
        // N/A mark not supported

        // Knio
        KFileInputStream.open(file).use { input ->
            assertTrue(input.markSupported())
        }
    }

    @Test
    fun `test reset without mark`(): Unit = runBlocking {
        val file = "src/test/resources/test.txt"

        // Java
        // N/A mark not supported

        // Knio
        KFileInputStream.open(file).use { input ->
            assertThrows<IOException> {
                runBlocking { input.reset() }
            }
        }
    }

    @Test
    fun `test skip beyond EOF`() = runBlocking {
        val file = "src/test/resources/test.txt"

        // Knio
        KFileInputStream.open(file).use { input ->
            val size = input.size()

            val skippedBytes = input.skip(10000)
            assertEquals(size, skippedBytes)
        }
    }

    @Test
    fun `test backward skip`() = runBlocking {
        val file = "src/test/resources/test.txt"

        // Java
        FileInputStream(file).use { input ->
            val buffer = ByteArray(5)
            input.read(buffer)
            val skippedBytes = input.skip(-5)
            assertEquals(-5L, skippedBytes)
        }

        // Knio
        KFileInputStream.open(file).use { input ->
            val buffer = ByteArray(5)
            input.read(buffer)
            val skippedBytes = input.skip(-5)
            assertEquals(-5L, skippedBytes)
        }
    }
}