package org.ivcode.org.ivcode.knio.io.core.htmlv1

import org.ivcode.knio.core.htmlv1.utils.urlDecode
import org.ivcode.knio.core.htmlv1.utils.urlEncode
import java.nio.charset.Charset

/**
 * Data class representing the head of an HTML request.
 *
 * @property method The HTTP method (e.g., GET, POST).
 * @property path The request path.
 * @property version The HTTP version.
 * @property headers A map of header names to their values.
 */
data class HtmlRequestHead (
    val method: String,
    val path: String,
    val version: String,
    val headers: Map<String, List<String>>
) {

    /**
     * Converts the request head to a byte array.
     *
     * @return The request head as a byte array.
     */
    fun toBytes(charset: Charset = Charsets.UTF_8): ByteArray {
        val sb = StringBuilder()
        sb.append("${method.urlEncode()} ${path.urlEncode()} ${version.urlEncode()}\r\n")
        headers.forEach { (name, values) ->
            values.forEach { value ->
                sb.append("${name.urlEncode()}: ${value.urlDecode()}\r\n")
            }
        }
        sb.append("\r\n")
        return sb.toString().toByteArray(charset)
    }
}