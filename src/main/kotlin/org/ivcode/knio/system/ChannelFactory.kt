package org.ivcode.knio.system

import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.file.OpenOption
import java.nio.file.Path

//TIP
// This is a factory interface that creates channels for file, server socket, and socket.
interface ChannelFactory {
    fun openFileChannel(file: Path, vararg options: OpenOption): AsynchronousFileChannel
    fun openServerSocketChannel(): AsynchronousServerSocketChannel
    fun openSocketChannel(): AsynchronousSocketChannel
    fun shutdown()
}