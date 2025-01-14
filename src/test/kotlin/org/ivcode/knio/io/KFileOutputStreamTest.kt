package org.ivcode.knio.io

import kotlinx.coroutines.runBlocking
import org.ivcode.knio.lang.use
import org.ivcode.knio.nio.knioOutputStream
import org.ivcode.knio.nio.md5
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

private val TEMP_DIR = Path("build/tmp")

class KFileOutputStreamTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "src/test/resources/test.txt",
    ])
    fun `java vs knio - output same md5`(dataSource: String):Unit = runBlocking {
        val data = File(dataSource).knioInputStream().reader().readText()

        val path1 = Files.createTempFile(TEMP_DIR, "test", ".txt")
        val path2 = Files.createTempFile(TEMP_DIR, "test", ".txt")
        try {
            path1.toFile().outputStream().use { fos ->
                fos.write(data.toByteArray())
            }

            path2.knioOutputStream().use { fos ->
                fos.write(data.toByteArray())
            }

            val javaMd5 = path1.md5()
            val knioMd5 = path2.md5()

            assertEquals(javaMd5, knioMd5)
        } finally {
            Files.delete(path1)
            Files.delete(path2)
        }
    }
}