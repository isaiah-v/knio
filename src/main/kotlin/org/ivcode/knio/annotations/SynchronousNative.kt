package org.ivcode.knio.annotations

/**
 * Indicates that the function relies on a synchronous operation backed by a native system call.
 * While this operation typically does not block the thread, it may experience delays if the system
 * is under heavy load or constrained by resources.
 *
 * Example: [AsynchronousFileChannel.size] is a synchronous native operation. It executes on the calling thread
 * and does not offload to another thread. However, delays may occur if the file system is busy or experiencing high
 * latency.
 *
 * In most cases, the operation is non-blocking, especially when working with NIO libraries, and does not require
 * special care. However, in high-scale applications, it is recommended to offload these operations to a dispatcher
 * dedicated to blocking tasks, allowing the main event loop to continue processing non-blocking tasks.
 *
 * This annotation acts as a hint to static analysis tools and developers to be cautious of potential delays in
 * system interactions.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Operation depends on a synchronous operation backed by a native system call."
)
annotation class SynchronousNative