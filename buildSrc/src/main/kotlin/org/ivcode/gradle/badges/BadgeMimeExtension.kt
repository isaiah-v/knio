package org.ivcode.gradle.badges

/**
 * The set of acceptable mime types
 */
enum class BadgeMimeTypes(
    val mimeType: String,
    val extension: String,
) {
    IMAGE_BIT("image/bmp", ".bmp"),
    IMAGE_GIF("image/gif", ".gif"),
    IMAGE_JPEG("image/jpeg", ".jpg"),
    IMAGE_PNG("image/png", ".png"),
    IMAGE_WEBP("image/webp", ".webp"),
    IMAGE_TIFF("image/tiff", ".tiff"),
    IMAGE_SVG("image/svg+xml", ".svg");

    companion object {
        fun getByMimeType(mimeType: String): BadgeMimeTypes? {
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