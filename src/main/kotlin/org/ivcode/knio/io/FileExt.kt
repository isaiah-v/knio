package org.ivcode.knio.io

import org.ivcode.knio.nio.knioInputStream
import org.ivcode.knio.nio.knioOutputStream
import org.ivcode.knio.nio.md5
import java.io.File

//NOTE: File Extensions should delegate operations to the Path Extensions

/**
 * Opens a `KInputStream` for the given `File`.
 *
 * @receiver The `File` to open the input stream for.
 * @return A `KInputStream` for the given `File`.
 */
suspend fun File.knioInputStream(): KInputStream = toPath().knioInputStream()

/**
 * Opens a `KOutputStream` for the given `File`.
 *
 * @receiver The `File` to open the output stream for.
 * @return A `KOutputStream` for the given `File`.
 */
suspend fun File.knioOutputStream(): KOutputStream = toPath().knioOutputStream()

/**
 * Computes the MD5 hash of the file.
 *
 * @receiver The `File` to compute the MD5 hash for.
 * @return The MD5 hash of the file as a hexadecimal string.
 */
suspend fun File.md5(): String = toPath().md5()