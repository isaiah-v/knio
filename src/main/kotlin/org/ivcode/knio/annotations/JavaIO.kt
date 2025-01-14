package org.ivcode.knio.annotations

/**
 * Indicates that the operation depends on the blocking java I/O api and may result in the thread blocking.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Operation depends on the blocking java I/O api and may result in thread blocking"
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class JavaIO
