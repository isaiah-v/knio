package org.ivcode.knio.nio

import org.ivcode.knio.io.*
import java.nio.file.Path


suspend fun Path.knioInputStream(): KInputStream {
    return KFileInputStream.open(this)
}

suspend fun Path.knioReader(): KReader {
    val inputStream = knioInputStream()
    return KInputStreamReader.open(inputStream)
}

suspend fun Path.knioBufferedReader(): KBufferedReader {
    val reader = knioReader()
    return KBufferedReader(reader)
}
