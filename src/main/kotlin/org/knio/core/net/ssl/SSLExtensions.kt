package org.knio.core.net.ssl

import org.knio.core.context.getKnioContext
import javax.net.ssl.SSLContext

// Related Extension Functions:
suspend fun SSLContext.getKnioSSLSocketFactory(): KSSLSocketFactory {
    return KSSLSocketFactoryDefault(this, getKnioContext())
}

suspend fun SSLContext.getKnioSSLServerSocketFactory(
): KSSLServerSocketFactory {
    return KSSLServerSocketFactoryDefault(this, getKnioContext())
}