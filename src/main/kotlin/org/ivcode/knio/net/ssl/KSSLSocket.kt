package org.ivcode.knio.net.ssl

import org.ivcode.knio.net.KSocket
import javax.net.ssl.HandshakeCompletedListener
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSession

interface KSSLSocket: KSocket {
    fun getEnabledCipherSuites(): Array<String>
    fun setEnabledCipherSuites(suites: Array<String>)
    fun getSupportedCipherSuites(): Array<String>
    fun getEnabledProtocols(): Array<String>
    fun setEnabledProtocols(protocols: Array<String>)
    fun setNeedClientAuth(need: Boolean)
    fun getNeedClientAuth(): Boolean
    fun setWantClientAuth(want: Boolean)
    fun getWantClientAuth(): Boolean
    fun setUseClientMode(mode: Boolean)
    fun getUseClientMode(): Boolean
    fun setEnableSessionCreation(flag: Boolean)
    fun getEnableSessionCreation(): Boolean
    suspend fun startHandshake()
    fun getSession(): SSLSession
    fun getApplicationProtocol(): String? // Added for TLS ALPN (Java 9+)
    fun getHandshakeApplicationProtocol(): String? // Added for TLS ALPN (Java 9+)
    fun setHandshakeApplicationProtocolSelector(selector: (KSSLSocket, MutableList<String>) -> String?) // Added for TLS ALPN (Java 9+)
    fun getHandshakeApplicationProtocolSelector(): ((KSSLSocket, MutableList<String>) -> String?)?
    fun getHandshakeSession(): SSLSession
    fun addHandshakeCompletedListener(listener: HandshakeCompletedListener)
    fun removeHandshakeCompletedListener(listener: HandshakeCompletedListener)
    fun getSSLParameters(): SSLParameters
    fun setSSLParameters(params: SSLParameters)
}