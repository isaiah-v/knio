package org.ivcode.knio.system

import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.file.OpenOption
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Default implementation of the AsynchronousChannelFactory interface.
 *
 * This factory creates various types of asynchronous channels using a provided executor service.
 *
 * @property executor The executor service used to manage asynchronous tasks.
 */
class ChannelFactoryDefault(
    private val executor: ExecutorService = Executors.newCachedThreadPool(ThreadFactoryVirtual("Kino"))
) : ChannelFactory {

    private val group: AsynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(executor)

    /**
     * Opens an asynchronous file channel.
     *
     * @param file The path to the file to be opened.
     * @param options The options specifying how the file is opened.
     * @return An AsynchronousFileChannel for the specified file.
     */
    override fun openFileChannel(file: Path, vararg options: OpenOption): AsynchronousFileChannel {
        return AsynchronousFileChannel.open(file, options.toSet(), executor)
    }

    /**
     * Opens an asynchronous server socket channel.
     *
     * @return An AsynchronousServerSocketChannel.
     */
    override fun openServerSocketChannel(): AsynchronousServerSocketChannel {
        return AsynchronousServerSocketChannel.open(group)
    }

    /**
     * Opens an asynchronous socket channel.
     *
     * @return An AsynchronousSocketChannel.
     */
    override fun openSocketChannel(): AsynchronousSocketChannel {
        return AsynchronousSocketChannel.open(group)
    }

    override fun shutdown() {
        group.shutdown()
        executor.shutdown()
    }
}