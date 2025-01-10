package org.ivcode.org.ivcode.knio.system

import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.file.OpenOption
import java.nio.file.Path

//TIP
// This is a factory interface that creates channels for file, server socket, and socket.
interface ChannelFactory {

    companion object {
        private var DEFAULT: ChannelFactory = ChannelFactoryDefault()

        fun getDefault(): ChannelFactory = DEFAULT
        fun setDefault(factory: ChannelFactory) {
            DEFAULT = factory
        }
    }

    fun openFileChannel(file: Path, vararg options: OpenOption): AsynchronousFileChannel
    fun openServerSocketChannel(): AsynchronousServerSocketChannel
    fun openSocketChannel(): AsynchronousSocketChannel
    fun shutdown()
}