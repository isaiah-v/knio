package org.ivcode.knio.annotations

import kotlinx.coroutines.Dispatchers
import org.ivcode.knio.context.KnioContext
import java.nio.channels.AsynchronousFileChannel
import javax.net.ssl.SSLEngine

/**
 * Indicates that the function may block the calling thread due to reliance on synchronous or native system operations.
 * This may result in waiting without active work being performed, especially in scenarios involving system load or
 * contention.
 *
 * The `Knio` library attempts to mitigate blocking by offloading such operations to a designated context, preventing
 * thread starvation in [Dispatchers.Default]. However, by default, these operations execute in the calling context unless
 * explicitly specified otherwise.
 *
 * Note that this annotation is to serve as documentation and a warning to developers. Adding the annotation to a
 * function does not change its behavior or execution.
 *
 * #### Examples
 * 1. **I/O Operations**:
 * [AsynchronousFileChannel.size] is a synchronous native operation. It runs on the calling thread and does not offload
 * to another thread. While it is typically non-blocking when working with NIO libraries, delays may occur if the file
 * system is under heavy load or experiencing high latency.
 *
 * 2. **CPU-Bound Work**:
 * [SSLEngine.wrap] performs active work on the CPU. While it may "block" a thread during execution, this blocking
 * occurs due to the work being performed, not due to waiting. CPU-bound operations like this are not considered blocking
 * under this annotation's context.
 *
 * ### Recommendations
 * In high-scale applications or systems under load, it is recommended to offload blocking operations to a dispatcher
 * configured for blocking tasks to avoid affecting the responsiveness of non-blocking coroutines.
 *
 * @see [KnioContext.blockingContext]
 */
@RequiresOptIn(
    message = "Operation may experience delays due to synchronous or native system calls.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class Blocking
