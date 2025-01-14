package org.ivcode.knio.net.ssl

import org.ivcode.knio.context.getKnioContext
import javax.net.ssl.SSLContext

// Related Extension Functions:
suspend fun SSLContext.getKnioSSLSocketFactory(): KSSLSocketFactory {
    return KSSLSocketFactoryDefault(this, getKnioContext())
}

suspend fun SSLContext.getKnioSSLServerSocketFactory(
): KSSLServerSocketFactory {
    @Suppress("BlockingMethodInNonBlockingContext")
    return KSSLServerSocketFactoryDefault(this, getKnioContext())
}