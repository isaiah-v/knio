package org.ivcode.knio.io

import java.io.File

suspend fun File.knioInputStream(): KFileInputStream {
    return KFileInputStream.open(this.path)
}