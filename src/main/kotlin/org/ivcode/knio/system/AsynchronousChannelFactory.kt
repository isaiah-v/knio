package org.ivcode.org.ivcode.knio.system

import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.file.OpenOption
import java.nio.file.Path

interface AsynchronousChannelFactory {

    companion object {
        var DEFAULT: AsynchronousChannelFactory = AsynchronousChannelFactoryDefault()


        fun getDefault(): AsynchronousChannelFactory = DEFAULT
        fun setDefault(factory: AsynchronousChannelFactory) {
            DEFAULT = factory
        }
    }

    fun openAsynchronousFileChannel(file: Path, vararg options: OpenOption): AsynchronousFileChannel
    fun openAsynchronousServerSocketChannel(): AsynchronousServerSocketChannel
    fun openAsynchronousSocketChannel(): AsynchronousSocketChannel
}