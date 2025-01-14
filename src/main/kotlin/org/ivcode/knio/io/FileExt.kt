package org.ivcode.knio.io

import org.ivcode.knio.nio.knioInputStream
import org.ivcode.knio.nio.md5
import java.io.File

suspend fun File.knioInputStream(): KInputStream = toPath().knioInputStream()
suspend fun File.md5(): String = toPath().md5()