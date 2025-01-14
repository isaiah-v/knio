package org.ivcode.knio.context

import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.file.OpenOption
import java.nio.file.Path

/**
 * Factory for creating channels
 */
interface ChannelFactory {
    fun openFileChannel(file: Path, vararg options: OpenOption): AsynchronousFileChannel
    fun openServerSocketChannel(): AsynchronousServerSocketChannel
    fun openSocketChannel(): AsynchronousSocketChannel
    fun shutdown()
}