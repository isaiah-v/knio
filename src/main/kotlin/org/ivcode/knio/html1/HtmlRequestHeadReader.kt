package org.ivcode.knio.html1

import org.ivcode.knio.Buffer
import org.ivcode.knio.Channel
import org.ivcode.knio.html1.utils.urlDecode

/**
 * Class responsible for reading the HTTP request head from a NIO channel.
 */
class HtmlRequestHeadReader {

    /**
     * Enum representing the different states of reading the request head.
     */
    private enum class ReadState {
        REQUEST_LINE,
        HEADER,
        DONE,
    }

    private var state = ReadState.REQUEST_LINE

    private var method: String? = null
    private var path: String? = null
    private var version: String? = null
    private var headers: MutableMap<String, MutableList<String>> = mutableMapOf()

    /**
     * Reads data from the channel until the request head is fully read.
     *
     * @param channel The NIO channel to read from.
     * @return True if reading is complete, false if more data is needed.
     */
    fun doRead(channel: Channel): Boolean {
        if(state == ReadState.DONE) {
            return true
        }

        do {
            val next = when (state) {
                ReadState.REQUEST_LINE -> ::readRequestLine
                ReadState.HEADER -> ::readHeaders
                ReadState.DONE -> return true
            }
        } while (next(channel))

        return isDone()
    }

    /**
     * Checks if the request head reading is complete.
     *
     * @return True if reading is complete, false otherwise.
     */
    fun isDone(): Boolean = state == ReadState.DONE

    /**
     * Builds the HtmlRequestHead object from the read data.
     *
     * @return The constructed HtmlRequestHead object.
     * @throws IllegalStateException if the request head reading is incomplete.
     */
    fun build(): HtmlRequestHead {
        if(!isDone()) {
            throw IllegalStateException("reading request head incomplete")
        }

        return HtmlRequestHead(method!!, path!!, version!!, headers)
    }

    /**
     * Reads the request line from the buffer.
     *
     * @param channel The NIO channel to read from.
     * @return True to continue reading, false to stop reading and wait for more data.
     */
    private fun readRequestLine(channel: Channel): Boolean {
        val reader = channel.reader()
        val buffer = reader.buffer

        read@ do {
            while (buffer.hasRemaining()) {
                // if null, the line is incomplete, and we need to read more
                val line = readLine(buffer) ?: continue@read

                val parts = line.split(" ")
                method =  parts[0].trim().urlDecode()
                path = parts[1].trim().urlDecode()
                version = parts[2].trim().urlDecode()

                state = ReadState.HEADER // move to reading headers

                // move to reading headers
                return true
            }
        } while (reader.read())

        // If we reach here, a read returned false before the request line was complete
        return false
    }

    /**
     * Reads the headers from the buffer.
     *
     * @param channel The NIO channel to read from.
     * @return True to continue reading, false to stop reading and wait for more data.
     */
    private fun readHeaders(channel: Channel): Boolean {
        val reader = channel.reader()
        val buffer = reader.buffer

        read@ do {
            while (buffer.hasRemaining()) {
                // if null, the line is incomplete, and we need to read more
                val line = readLine(reader.buffer) ?: continue@read

                val header = parseHeader(line)
                if (header != null) {
                    headers.getOrPut(header.first.urlDecode()) { mutableListOf() }.add(header.second.urlDecode())
                } else {
                    state = ReadState.DONE
                    return true
                }
            }
        } while (reader.read())

        // If we reach here, a read returned false before the headers were complete
        return false
    }

    /**
     * Parses a header line into a key-value pair.
     *
     * @param line The header line to parse.
     * @return A pair containing the header key and value, or null if the line is empty.
     * @throws IllegalArgumentException if the header line is invalid.
     */
    private fun parseHeader(line: String): Pair<String, String>? {
        if(line.isEmpty()) {
            // End of headers
            this.state = ReadState.DONE
            return null
        }

        val parts = line.split(":")
        if(parts.size < 2) {
            throw IllegalArgumentException("Invalid header: $line")
        }

        val key = parts[0].trim()
        val value = parts[1].trim()

        return key to value
    }

    /**
     * Reads a line from the ByteBuffer, supporting partial lines.
     * Marks the start of the line and allows for continuation if more data is read.
     *
     * @param buffer The buffer to read the line from.
     * @return The read line as a String, or null if the line is incomplete.
     */
    private fun readLine(buffer: Buffer): String? {
        val lineBuilder = StringBuilder()

        // Save the current position to mark the start of the line
        val initialPosition = buffer.position()

        while (buffer.hasRemaining()) {
            val currentByte = buffer.get()

            // Check for line breaks
            when (currentByte.toInt().toChar()) {
                '\r' -> {
                    // Check if the next byte is '\n' (carriage return + newline)
                    if (buffer.hasRemaining() && buffer.get(buffer.position()) == '\n'.code.toByte()) {
                        buffer.get() // consume the '\n'
                        return lineBuilder.toString()
                    }
                    // If it's just '\r', return the current line
                    return lineBuilder.toString()
                }
                '\n' -> {
                    return lineBuilder.toString()
                }
                else -> {
                    // Otherwise, append the byte to the line
                    lineBuilder.append(currentByte.toInt().toChar())
                }
            }
        }

        // If we reach here, the line is not complete; mark the position
        // and return null, meaning the line is incomplete.
        buffer.position(initialPosition) // Reset position to start of the line

        return null
    }
}