package org.ivcode.knio.utils

import org.ivcode.knio.Buffer
import java.nio.ByteBuffer
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult

fun SSLEngine.wrap(src: Buffer, dst: Buffer): SSLEngineResult = this.wrap(src.buffer(), dst.buffer())
fun SSLEngine.wrap(src: ByteBuffer, dst: Buffer): SSLEngineResult = this.wrap(src, dst.buffer())
fun SSLEngine.unwrap(src: Buffer, dst: Buffer): SSLEngineResult = this.unwrap(src.buffer(), dst.buffer())