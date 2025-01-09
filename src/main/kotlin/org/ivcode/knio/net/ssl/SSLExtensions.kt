package org.ivcode.knio.net.ssl

import javax.net.ssl.SSLContext

// Related Extension Functions:
fun SSLContext.getKnioSSLSocketFactory(): KSSLSocketFactory {
    return KSSLSocketFactoryDefault(this)
}