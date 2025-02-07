package org.knio.core.annotations

/**
 * Marks a function as not suspended.
 *
 * Servers as a warning within the project that a function is not suspended.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This function is not suspended."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
internal annotation class NotSuspended()
