package org.knio.core.lang

import org.knio.core.io.KReader
import org.knio.core.io.KStringReader
import java.nio.CharBuffer

fun String.toCharBuffer(): CharBuffer = CharBuffer.wrap(this)
suspend fun String.knioReader(): KReader = KStringReader.open(this)