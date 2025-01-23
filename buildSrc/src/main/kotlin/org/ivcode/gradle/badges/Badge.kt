package org.ivcode.gradle.badges.model

const val GREEN = "green"
const val RED = "red"

data class Badge (
    val label: String,
    val message: String,
    val color: String,
    val link: String?,
)

internal fun Badge.toMarkdown(): String {
    if(label.isBlank())
        return "[${label}](https://img.shields.io/badge/${label}-${message}-${color})"
    else {
        return "[[${label}](https://img.shields.io/badge/${label}-${message}-${color})]($link)"
    }
}

private fun encodeValue(value: String): String {
    val sb = StringBuilder()

    for (i in 0..value.length) {
        val ch = value[i]
        when (ch) {
            '_', '-' -> {
                sb.append(ch).append(ch)
            }
            else -> {
                sb.append(ch)
            }
        }
    }
}