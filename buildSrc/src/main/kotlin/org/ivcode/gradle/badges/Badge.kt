package org.ivcode.gradle.badges

import java.net.URI
import java.net.URLEncoder

const val GREEN = "green"
const val RED = "red"

data class Badge (
    val label: String,
    val message: String,
    val color: String,
    val link: URI?,
)


internal fun Badge.toUri(): URI {
    return URI.create("https://img.shields.io/badge/${badgeContent()}")
}

private fun Badge.badgeContent(): String {
    return URLEncoder.encode (
        "${encodeBadgeContent(label)}-${encodeBadgeContent(message)}-${color}",
        Charsets.UTF_8
    ).replace("+", "%20")
}

private fun encodeBadgeContent(value: String): String {
    val sb = StringBuilder()

    for (element in value) {
        when (element) {
            '_', '-' -> {
                sb.append(element).append(element)
            }
            else -> {
                sb.append(element)
            }
        }
    }

    return sb.toString()
}