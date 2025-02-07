package org.knio.core.lang

import org.knio.core.io.KByteArrayInputStream

suspend fun ByteArray.knioInputStream(): KByteArrayInputStream = KByteArrayInputStream.open(this)