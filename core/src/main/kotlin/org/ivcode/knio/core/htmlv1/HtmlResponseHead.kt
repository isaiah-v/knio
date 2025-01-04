package org.ivcode.knio.core.htmlv1

import org.ivcode.knio.core.htmlv1.utils.urlEncode
import java.nio.charset.Charset

private const val UNKNOWN_STATUS_MESSAGE = "Unknown"

data class HtmlResponseHead (
    val version: String,
    val status: Int,
    val message: String?,
    val headers: Map<String, List<String>>
) {
    fun toBytes(charset: Charset = Charsets.UTF_8): ByteArray {
        val sb = StringBuilder()
        sb.append(version.urlEncode())
        sb.append(" ")
        sb.append(status)
        if(message != null) {
            sb.append(" ")
            sb.append(message.urlEncode())
        } else {
            val httpStatus = HttpStatusCode.fromCode(status)
            if(httpStatus != null) {
                sb.append(" ")
                sb.append(httpStatus.reasonPhrase.urlEncode())
            } else {
                sb.append(" ")
                sb.append(UNKNOWN_STATUS_MESSAGE.urlEncode())
            }
        }
        sb.append("\r\n")
        for((name, values) in headers) {
            for(value in values) {
                sb.append(name.urlEncode())
                sb.append(": ")
                sb.append(value.urlEncode())
                sb.append("\r\n")
            }
        }
        sb.append("\r\n")
        return sb.toString().toByteArray(charset)
    }
}
