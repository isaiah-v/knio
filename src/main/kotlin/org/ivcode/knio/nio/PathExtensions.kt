package org.ivcode.org.ivcode.knio.nio

import org.ivcode.knio.io.KFileInputStream
import org.ivcode.knio.io.KInputStream
import org.ivcode.knio.io.KInputStreamReader
import org.ivcode.knio.io.KReader
import java.nio.file.Path

suspend fun Path.inputStream(): KInputStream {
    return KFileInputStream.open(this)
}

suspend fun Path.reader(): KReader {
    val inputStream = inputStream()
    return KInputStreamReader.open(inputStream)
}
