package org.ivcode.knio.io

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.annotations.JavaIO
import org.ivcode.knio.lang.use
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer


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
                    @OptIn(JavaIO::class)
                    fis.copyTo(outputStream)
                }

                outputStream.toByteArray()
            }
        }

        val expected = expectedExec()
        val actual = runBlocking { actualExec() }

        assertTrue(expected.contentEquals(actual))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "src/test/resources/test.txt",
    ])
    fun `mark and reset work correctly`(file: String) = runBlocking {
        val kFileInputStream = KFileInputStream.open(file)
        kFileInputStream.mark(10)
        val buffer = ByteBuffer.allocate(5)
        kFileInputStream.read(buffer)
        kFileInputStream.reset()
        val resetBuffer = ByteBuffer.allocate(5)
        kFileInputStream.read(resetBuffer)
        assertTrue(buffer.array().contentEquals(resetBuffer.array()))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "src/test/resources/test.txt",
    ])
    fun `skip skips the correct number of bytes`(file: String) = runBlocking {
        val kFileInputStream = KFileInputStream.open(file)
        val skippedBytes = kFileInputStream.skip(5)
        assertTrue(skippedBytes == 5L)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "src/test/resources/test.txt",
    ])
    fun `read reads the correct number of bytes`(file: String) = runBlocking {
        val kFileInputStream = KFileInputStream.open(file)
        val buffer = ByteBuffer.allocate(5)
        val bytesRead = kFileInputStream.read(buffer)
        assertTrue(bytesRead == 5)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "src/test/resources/test.txt",
    ])
    fun `close closes the file input stream`(file: String) = runBlocking {
        val kFileInputStream = KFileInputStream.open(file)
        kFileInputStream.close()
        assertThrows<IOException> {
            runBlocking { kFileInputStream.read(ByteBuffer.allocate(5)) }
        }
    }
}