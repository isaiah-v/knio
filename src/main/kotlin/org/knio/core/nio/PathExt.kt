package org.knio.core.nio

import org.knio.core.io.*
import org.knio.core.lang.use
import org.knio.core.security.update
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.inputStream

/**
 * Opens a `KInputStream` for the given `Path`.
 *
 * @receiver The `Path` to open the input stream for.
 * @return A `KInputStream` for the given `Path`.
 */
suspend fun Path.knioInputStream(): KInputStream {
    return KFileInputStream.open(this)
}

/**
 * Opens a `KOutputStream` for the given `Path`.
 *
 * @receiver The `Path` to open the output stream for.
 * @return A `KOutputStream` for the given `Path`.
 */
suspend fun Path.knioOutputStream(): KOutputStream {
    return KFileOutputStream.open(this)
}

/**
 * Computes the MD5 hash of the file at the given `Path`.
 *
 * @receiver The `Path` of the file to compute the MD5 hash for.
 * @return The MD5 hash of the file as a hexadecimal string.
 */
suspend fun Path.md5(): String {
    val digest = MessageDigest.getInstance("MD5")

    knioInputStream().use { fis ->
        digest.update(fis)
    }

    val bytes = digest.digest()
    return bytes.joinToString("") { "%02x".format(it) }
}