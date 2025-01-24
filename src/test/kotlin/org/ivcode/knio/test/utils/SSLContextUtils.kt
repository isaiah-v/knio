package org.ivcode.knio.test.utils

import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.*

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

fun createSSLContext (
    protocol: String = "TLS",
    random: SecureRandom? = null,
    keystore: String? = null,
    keystorePassword: String? = null,
    truststore: String? = null,
    truststorePassword: String? = null
): SSLContext {

    val keyManagerFactory = keystore?.let {
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(File(it).inputStream(), keystorePassword?.toCharArray())
        keyManagerFactory.init(keyStore, keystorePassword?.toCharArray() ?: "changeit".toCharArray())

        keyManagerFactory
    }

    val trustManagerFactory = truststore?.let {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
        trustStore.load(File(it).inputStream(), truststorePassword?.toCharArray() ?: "changeit".toCharArray())
        trustManagerFactory.init(trustStore)

        trustManagerFactory
    }

    val sslContext = SSLContext.getInstance(protocol)
    sslContext.init(keyManagerFactory?.keyManagers, trustManagerFactory?.trustManagers, random)

    return sslContext

}

fun createTrustAllSSLContext(): SSLContext {
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, createTrustAllManagers(), null)

    return sslContext
}