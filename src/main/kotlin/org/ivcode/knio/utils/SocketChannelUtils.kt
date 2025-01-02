package org.ivcode.knio.utils

import org.ivcode.knio.Buffer
import java.nio.channels.SocketChannel

fun SocketChannel.write(src: Buffer): Int = this.write(src.buffer())
fun SocketChannel.read(dst: Buffer): Int = this.read(dst.buffer())
