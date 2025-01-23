package org.ivcode.gradle.badges

const val EXTENSION_SVG = ".svg"

/**
 * The set of acceptable mime types
 */
enum class BadgeMimeExtension(
    val mimeType: String,
    val extension: String,
) {
    IMAGE_SVG("image/svg+xml", EXTENSION_SVG);

    companion object {
        fun getByMimeType(mimeType: String): BadgeMimeExtension? {
            val type = mimeType.lowercase()

            for (mime in values()) {
                if(mime.mimeType == type) {
                    return mime
                }
            }

            return null
        }
    }
}