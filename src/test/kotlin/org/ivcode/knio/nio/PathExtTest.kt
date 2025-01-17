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
        // .p12 file with known md5 hash.
        "src/test/resources/keystore.p12, fb22103ff7f13daafdc67da5aff731d1"
    ])
    fun `md5 test`(input: String, expected: String) = runBlocking {
        // Note: git modifies the line endings based on the OS.
        // Known md5 should only be generated on binary file types.

        val actual = Path.of(input).md5()
        assertEquals(expected.uppercase(), actual.uppercase())
    }

    @Test
    fun `test md5 variance`() = runBlocking {
        // A test to ensure different files have different md5 hashes

        val md5_1 = Path.of("src/test/resources/test.txt").md5()
        val md5_2 = Path.of("src/test/resources/meta.md").md5()

        assertNotEquals(md5_1, md5_2)
    }
}