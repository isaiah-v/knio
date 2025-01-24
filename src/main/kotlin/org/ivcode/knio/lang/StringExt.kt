package org.ivcode.knio.lang

import org.ivcode.knio.io.KReader
import org.ivcode.knio.io.KStringReader
import java.nio.CharBuffer

fun String.toCharBuffer(): CharBuffer = CharBuffer.wrap(this)
suspend fun String.knioReader(): KReader = KStringReader.open(this)