package org.ivcode.knio.io

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ivcode.knio.context.getKnioContext
import org.ivcode.knio.nio.writeSuspend
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class KFileOutputStream private constructor (
    private val fileChannel: AsynchronousFileChannel
): KOutputStream() {

    private val mutex = Mutex()
    private var position: Long = 0

    companion object {
        suspend fun open(path: Path): KFileOutputStream {
            val context = getKnioContext()

            return KFileOutputStream(
                context.channelFactory.openFileChannel(path, StandardOpenOption.WRITE)
            )
        }
    }

    override suspend fun write(b: ByteBuffer) = mutex.withLock {
        write0(b)
    }

    private suspend fun write0(b: ByteBuffer) {
        while (b.hasRemaining()) {
            val read = fileChannel.writeSuspend(b, position)

            if (read == -1 || read == 0) {
                break
            } else {
                position += read
            }
        }
    }

    override suspend fun close() = mutex.withLock {
        fileChannel.close()
    }
}