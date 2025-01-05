package org.ivcode.org.ivcode.knio.io.core

/**
 * Interface representing a reader for NIO (Non-blocking I/O) connections.
 *
 * This interface provides a way to read data from a connection in a non-blocking manner.
 * Data is read into the buffer provided by the implementation.
 *
 * The buffer's position is managed by the user, allowing data to remain in the buffer between reads. However, the buffer
 * should be cleared, compressed, or otherwise managed to prevent it from becoming full.
 *
 * Note that this is a simple wrapper around the NIO read operation. Keep in mind that the buffer may have already been
 * read into by the time the read event is triggered. For example, the SSL/TLS implementation may read data into the
 * buffer while performing the handshake, or the HTTP implementation may read data into the buffer while parsing the
 * headers. This means if a read event is triggered, the buffer may already contain data for processing.
 */
interface Reader {

    /**
     * The buffer that data is read into.
     */
    val buffer: Buffer

    /**
     * Performs a non-blocking read operation, reading data from the connection into the buffer.
     *
     * @return True if data was read, even if none was written into the buffer, false otherwise if no data was read.
     * @throws ClosedSocketException If the connection is closed.
     */
    fun read(): Boolean
}