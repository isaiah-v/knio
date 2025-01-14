package org.ivcode.knio.nio

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Path

class PathExtTest {

    @ParameterizedTest
    @CsvSource(value = [
        "src/test/resources/test.txt, D24E481393F6E4BECA4A985DAA3C57DD"
    ])
    fun `md5 test`(input: String, expected: String) = runBlocking {
        val actual = Path.of(input).md5().uppercase()
        assertEquals(expected, actual)
    }

    @Test
    fun `test md5 variance`() = runBlocking {
        // A test to ensure different files have different md5 hashes
        val md5_1 = Path.of("src/test/resources/test.txt").md5()
        val md5_2 = Path.of("src/test/resources/meta.md").md5()

        assertNotEquals(md5_1, md5_2)
    }
}