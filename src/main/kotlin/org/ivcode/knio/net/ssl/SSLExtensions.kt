package org.ivcode.knio.net.ssl

import org.ivcode.knio.system.knioContext
import javax.net.ssl.SSLContext

// Related Extension Functions:
suspend fun SSLContext.getKnioSSLSocketFactory(): KSSLSocketFactory {
    return KSSLSocketFactoryDefault(this, knioContext())
}

suspend fun SSLContext.getKnioSSLServerSocketFactory(
): KSSLServerSocketFactory {
    @Suppress("BlockingMethodInNonBlockingContext")
    return KSSLServerSocketFactoryDefault(this, knioContext())
}