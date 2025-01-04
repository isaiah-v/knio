package org.ivcode.knio.core.htmlv1

import java.nio.ByteBuffer

/**
 * Interface representing a handler for HTML responses in a NIO context.
 */
interface NioHtmlResponseHandler {

    /**
     * Sets the status code and message for the response.
     * Cannot be called once the body has been written to.
     *
     * @param statusCode The status code for the response.
     * @param statusMessage The status message for the response.
     */
    fun setStatusCode(statusCode: Int, statusMessage: String? = null)

    /**
     * Adds a header to the response.
     * Cannot be called once the body has been written to.
     *
     * @param name The name of the header.
     * @param value The value of the header.
     */
    fun addHeader(name: String, value: String)

    /**
     * Called when data is to be written to the response as part of the body.
     *
     * The data written will be the remaining data in the buffer.
     *
     * @param data The data to write to the response.
     */
    fun onResponseWrite(data: ByteBuffer)

    /**
     * Called when the response is completed.
     */
    fun onResponseEnd()
}