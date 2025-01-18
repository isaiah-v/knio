package org.ivcode.knio.annotations

/**
 * Indicates that the operation depends on the blocking Java I/O API and may result in the thread blocking.
 *
 * Not every Java I/O class is blocking. For example, [java.io.StringReader] is non-blocking, while
 * [java.io.FileReader] is blocking. This annotation is used to indicate that the operation may result
 * in thread blocking, and special care should be taken when using it.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Operation depends on the blocking Java I/O API and may result in thread blocking"
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class JavaIO