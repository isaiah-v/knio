package org.ivcode.knio.nio

import org.ivcode.knio.io.*
import java.nio.file.Path

suspend fun Path.knioInputStream(): KInputStream {
    return KFileInputStream.open(this)
}

