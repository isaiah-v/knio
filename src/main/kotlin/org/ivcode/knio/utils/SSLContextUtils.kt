package org.ivcode.org.ivcode.knio.utils

import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Creates an array of TrustManagers that trust all certificates.
 *
 * @return An array of TrustManagers that trust all certificates.
 */
fun createTrustAllManagers(): Array<TrustManager> {
    return arrayOf(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
    })
}

fun createTrustAllSSLContext(): SSLContext {
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, createTrustAllManagers(), null)

    return sslContext
}