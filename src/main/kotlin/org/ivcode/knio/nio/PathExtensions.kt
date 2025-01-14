package org.ivcode.knio.nio

import org.ivcode.knio.io.*
import org.ivcode.knio.lang.use
import org.ivcode.knio.security.update
import java.nio.file.Path
import java.security.MessageDigest

suspend fun Path.knioInputStream(): KInputStream {
    return KFileInputStream.open(this)
}

suspend fun Path.knioOutputStream(): KOutputStream {
    return KFileOutputStream.open(this)
}

suspend fun Path.md5(): String {
    val digest = MessageDigest.getInstance("MD5")

    knioInputStream().use { fis ->
        digest.update(fis)
    }

    val bytes = digest.digest()
    return bytes.joinToString("") { "%02x".format(it) }
}