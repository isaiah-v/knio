package org.ivcode.knio.utils

import javax.net.ssl.SSLEngineResult

/**
 * Extension function to check if the SSLEngineResult.Status is OK.
 *
 * @return True if the status is OK, false otherwise.
 */
fun SSLEngineResult.Status.isOk(): Boolean = this == SSLEngineResult.Status.OK

/**
 * Extension function to check if the SSLEngineResult.Status is CLOSED.
 *
 * @return True if the status is CLOSED, false otherwise.
 */
fun SSLEngineResult.Status.isClosed(): Boolean = this == SSLEngineResult.Status.CLOSED

/**
 * Extension function to check if the SSLEngineResult.Status is BUFFER_UNDERFLOW.
 *
 * @return True if the status is BUFFER_UNDERFLOW, false otherwise.
 */
fun SSLEngineResult.Status.isBufferUnderflow(): Boolean = this == SSLEngineResult.Status.BUFFER_UNDERFLOW

/**
 * Extension function to check if the SSLEngineResult.Status is BUFFER_OVERFLOW.
 *
 * @return True if the status is BUFFER_OVERFLOW, false otherwise.
 */
fun SSLEngineResult.Status.isBufferOverflow(): Boolean = this == SSLEngineResult.Status.BUFFER_OVERFLOW

/**
 * Executes the given block if the SSLEngineResult.Status is OK.
 *
 * @param block The block of code to execute.
 * @return The SSLEngineResult instance.
 */
fun SSLEngineResult.onStatusOk(block: SSLEngineResult.() -> Unit): SSLEngineResult = apply {
    if (status == SSLEngineResult.Status.OK) {
        block()
    }
}

/**
 * Executes the given block if the SSLEngineResult.Status is CLOSED.
 *
 * @param block The block of code to execute.
 * @return The SSLEngineResult instance.
 */
fun SSLEngineResult.onStatusClosed(block: SSLEngineResult.() -> Unit): SSLEngineResult = apply {
    if (status == SSLEngineResult.Status.CLOSED) {
        block()
    }
}

/**
 * Executes the given block if the SSLEngineResult.Status is BUFFER_UNDERFLOW.
 *
 * @param block The block of code to execute.
 * @return The SSLEngineResult instance.
 */
fun SSLEngineResult.onStatusBufferUnderflow(block: SSLEngineResult.() -> Unit): SSLEngineResult = apply {
    if (status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
        block()
    }
}

/**
 * Executes the given block if the SSLEngineResult.Status is BUFFER_OVERFLOW.
 *
 * @param block The block of code to execute.
 * @return The SSLEngineResult instance.
 */
fun SSLEngineResult.onStatusBufferOverflow(block: SSLEngineResult.() -> Unit): SSLEngineResult = apply {
    if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
        block()
    }
}

/**
 * Extension function to check if the SSLEngineResult.HandshakeStatus is NOT_HANDSHAKING.
 *
 * @return True if the handshake status is NOT_HANDSHAKING, false otherwise.
 */
fun SSLEngineResult.HandshakeStatus.isNotHandshaking(): Boolean = this == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING

/**
 * Extension function to check if the SSLEngineResult.HandshakeStatus is FINISHED.
 *
 * @return True if the handshake status is FINISHED, false otherwise.
 */
fun SSLEngineResult.HandshakeStatus.isFinished(): Boolean = this == SSLEngineResult.HandshakeStatus.FINISHED

/**
 * Extension function to check if the SSLEngineResult.HandshakeStatus is NEED_TASK.
 *
 * @return True if the handshake status is NEED_TASK, false otherwise.
 */
fun SSLEngineResult.HandshakeStatus.isNeedTask(): Boolean = this == SSLEngineResult.HandshakeStatus.NEED_TASK

/**
 * Extension function to check if the SSLEngineResult.HandshakeStatus is NEED_WRAP.
 *
 * @return True if the handshake status is NEED_WRAP, false otherwise.
 */
fun SSLEngineResult.HandshakeStatus.isNeedWrap(): Boolean = this == SSLEngineResult.HandshakeStatus.NEED_WRAP

/**
 * Extension function to check if the SSLEngineResult.HandshakeStatus is NEED_UNWRAP.
 *
 * @return True if the handshake status is NEED_UNWRAP, false otherwise.
 */
fun SSLEngineResult.HandshakeStatus.isNeedUnwrap(): Boolean = this == SSLEngineResult.HandshakeStatus.NEED_UNWRAP

/**
 * Extension function to check if the SSLEngineResult.HandshakeStatus is NEED_UNWRAP_AGAIN.
 *
 * @return True if the handshake status is NEED_UNWRAP_AGAIN, false otherwise.
 */
fun SSLEngineResult.HandshakeStatus.isNeedUnwrapAgain(): Boolean = this == SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN