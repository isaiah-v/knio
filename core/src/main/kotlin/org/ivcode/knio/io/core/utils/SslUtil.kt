package org.ivcode.org.ivcode.knio.io.core.utils

import org.ivcode.knio.core.model.SecurityStoreProperties
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.*

/**
 * Creates an SSLContext.
 *
 * @param keyManagerFactory The key manager factory. If null, the context will not have a key manager and will default to the system key store.
 * @param trustManagerFactory The trust manager factory. If null, the context will not have a trust manager and will default to the system trust store.
 * @return A configured SSLContext instance.
 */
private fun createSSLContext(
    keyManagers: Array<KeyManager>? = null,
    trustManagers: Array<TrustManager>? = null
): SSLContext {
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagers, trustManagers, null)
    return sslContext
}

fun createSSLContext (
    keystore: SecurityStoreProperties? = null,
    truststore: SecurityStoreProperties? = null
): SSLContext =
    createSSLContext(keystore?.toKeyManagers(), truststore?.toTrustManagers())

/**
 * Creates a KeyManagerFactory from the given security store properties.
 *
 * @param properties The properties of the security store. If null, returns null.
 * @return A configured KeyManagerFactory instance, or null if properties are null.
 */
fun createKeyManagerFactory(properties: SecurityStoreProperties?): KeyManagerFactory? = properties?.let {
    val keystore = KeyStore.getInstance(properties.type)
    keystore.load(FileInputStream(properties.path), properties.password.toCharArray())

    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    kmf.init(keystore, properties.password.toCharArray())

    kmf
}

/**
 * Extension function to convert SecurityStoreProperties to an array of KeyManagers.
 *
 * @return An array of KeyManagers, or null if the properties are null.
 */
fun SecurityStoreProperties.toKeyManagers(): Array<KeyManager>? {
    val kmf = createKeyManagerFactory(this) ?: return null
    return kmf.keyManagers
}

/**
 * Creates a TrustManagerFactory from the given security store properties.
 *
 * @param properties The properties of the security store. If null, returns null.
 * @return A configured TrustManagerFactory instance, or null if properties are null.
 */
fun createTrustManagerFactory(properties: SecurityStoreProperties?): TrustManagerFactory? = properties?.let {
    val trustStore = KeyStore.getInstance(properties.type)
    trustStore.load(FileInputStream(properties.path), properties.password.toCharArray())

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(trustStore)

    tmf
}

/**
 * Extension function to convert SecurityStoreProperties to an array of TrustManagers.
 *
 * @return An array of TrustManagers, or null if the properties are null.
 */
fun SecurityStoreProperties.toTrustManagers(): Array<TrustManager>? {
    val tmf = createTrustManagerFactory(this) ?: return null
    return tmf.trustManagers
}

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