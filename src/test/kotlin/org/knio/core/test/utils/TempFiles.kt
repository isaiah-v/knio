package org.knio.core.test.utils

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

private val TEMP_DIR = Path("build/tmp")
private const val TEMP_PREFIX = "test"
private const val TEMP_SUFFIX = ".txt"

fun mkTemp(): Path {
    return Files.createTempFile(TEMP_DIR, TEMP_PREFIX, TEMP_SUFFIX)
}