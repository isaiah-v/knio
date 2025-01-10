package org.ivcode.knio.net

import org.ivcode.knio.io.KInputStream
import org.jetbrains.annotations.Blocking

/**
 *
 *
 * [Blocking] - Though the underlying nio implementation is non-blocking, some operations may block briefly. In most
 * cases this will be negligible. It's important to note the underlying does not set the dispatcher for operations.
 * It is up to the user to decide the appropriate dispatcher.
 *
 */
@Blocking
abstract class KSocketInputStream: KInputStream()