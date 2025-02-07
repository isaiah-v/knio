package org.knio.core.io

import kotlinx.coroutines.runBlocking
import org.knio.core.lang.use
import org.knio.core.nio.knioOutputStream
import org.knio.core.nio.md5
import org.knio.core.test.utils.mkTemp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.io.path.deleteIfExists

class KFileOutputStreamTest {

    @ParameterizedTest
    @ValueSource(strings = [
        "src/test/resources/test.txt",
    ])
    fun `java vs knio - output same md5`(dataSource: String):Unit = runBlocking {
        val data = File(dataSource).knioInputStream().readAllBytes()

        val path1 = mkTemp()
        path1.toFile().outputStream().use { fos ->
            fos.write(data)
        }
        val javaMd5 = path1.md5()
        path1.deleteIfExists()


        val path2 = mkTemp()
        path2.knioOutputStream().use { fos ->
            fos.write(data)
        }
        val knioMd5 = path2.md5()
        path2.deleteIfExists()

        assertEquals(javaMd5, knioMd5)
    }
}