package org.ivcode.knio.html1.utils

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun String.urlDecode(): String {
    return URLDecoder.decode(this, StandardCharsets.UTF_8.toString())
}

fun String.urlEncode(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
}
