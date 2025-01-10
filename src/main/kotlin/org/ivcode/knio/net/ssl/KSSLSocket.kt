package org.ivcode.knio.net.ssl

import org.ivcode.knio.net.KSocket
import org.jetbrains.annotations.Blocking
import javax.net.ssl.HandshakeCompletedListener
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSession

@Blocking
interface KSSLSocket: KSocket {

    suspend fun startHandshake()
    suspend fun getEnabledCipherSuites(): Array<String>
    suspend fun setEnabledCipherSuites(suites: Array<String>)
    suspend fun getSupportedCipherSuites(): Array<String>
    suspend fun getEnabledProtocols(): Array<String>
    suspend fun setEnabledProtocols(protocols: Array<String>)
    suspend fun setNeedClientAuth(need: Boolean)
    suspend fun getNeedClientAuth(): Boolean
    suspend fun setWantClientAuth(want: Boolean)
    suspend fun getWantClientAuth(): Boolean
    suspend fun setUseClientMode(mode: Boolean)
    suspend fun getUseClientMode(): Boolean
    suspend fun setEnableSessionCreation(flag: Boolean)
    suspend fun getEnableSessionCreation(): Boolean
    suspend fun getSession(): SSLSession
    suspend fun getApplicationProtocol(): String? // Added for TLS ALPN (Java 9+)
    suspend fun getHandshakeApplicationProtocol(): String? // Added for TLS ALPN (Java 9+)
    suspend fun setHandshakeApplicationProtocolSelector(selector: (KSSLSocket, MutableList<String>) -> String?) // Added for TLS ALPN (Java 9+)
    suspend fun getHandshakeApplicationProtocolSelector(): ((KSSLSocket, MutableList<String>) -> String?)?
    suspend fun getHandshakeSession(): SSLSession
    suspend fun addHandshakeCompletedListener(listener: HandshakeCompletedListener)
    suspend fun removeHandshakeCompletedListener(listener: HandshakeCompletedListener)
    suspend fun getSSLParameters(): SSLParameters
    suspend fun setSSLParameters(params: SSLParameters)
    suspend fun getSupportedProtocols(): Array<String>
}