package org.ivcode.knio.context

import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.file.OpenOption
import java.nio.file.Path

/**
 * Factory for creating asynchronous I/O channels.
 */
interface ChannelFactory {
    fun openFileChannel(file: Path, vararg options: OpenOption): AsynchronousFileChannel
    fun openServerSocketChannel(): AsynchronousServerSocketChannel
    fun openSocketChannel(): AsynchronousSocketChannel

    /**
     * Shuts down the factory and releases any resources.
     */
    fun shutdown()
}