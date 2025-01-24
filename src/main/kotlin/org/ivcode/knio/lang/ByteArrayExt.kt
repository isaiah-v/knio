package org.ivcode.knio.lang

import org.ivcode.knio.io.KByteArrayInputStream

suspend fun ByteArray.knioInputStream(): KByteArrayInputStream = KByteArrayInputStream.open(this)