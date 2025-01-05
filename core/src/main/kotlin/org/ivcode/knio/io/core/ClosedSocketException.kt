package org.ivcode.org.ivcode.knio.io.core

/**
 * Exception thrown when a socket is closed unexpectedly.
 */
class ClosedSocketException: RuntimeException {

    /**
     * Constructs a new ClosedSocketException with null as its detail message.
     */
    constructor(): super()

    /**
     * Constructs a new ClosedSocketException with the specified detail message.
     *
     * @param message The detail message.
     */
    constructor(message: String): super(message)

    /**
     * Constructs a new ClosedSocketException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    constructor(message: String, cause: Throwable): super(message, cause)

    /**
     * Constructs a new ClosedSocketException with the specified cause.
     *
     * @param cause The cause of the exception.
     */
    constructor(cause: Throwable): super(cause)

    /**
     * Constructs a new ClosedSocketException with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     *
     * @param message The detail message.
     * @param cause The cause of the exception.
     * @param enableSuppression Whether or not suppression is enabled or disabled.
     * @param writableStackTrace Whether or not the stack trace should be writable.
     */
    constructor(message: String, cause: Throwable, enableSuppression: Boolean, writableStackTrace: Boolean): super(message, cause, enableSuppression, writableStackTrace)
}