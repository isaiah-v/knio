package org.knio.core.net.ssl

import org.knio.core.net.KServerSocket
import javax.net.ssl.SSLParameters

interface KSSLServerSocket: KServerSocket {
    override suspend fun accept(): KSSLSocket
    suspend fun getEnabledCipherSuites(): Array<String>
    suspend fun getEnabledProtocols(): Array<String>
    suspend fun getEnableSessionCreation(): Boolean
    suspend fun getNeedClientAuth(): Boolean
    suspend fun getSSLParameters(): SSLParameters
    suspend fun getSupportedCipherSuites(): Array<String>
    suspend fun getSupportedProtocols(): Array<String>
    suspend fun getUseClientMode(): Boolean
    suspend fun getWantClientAuth(): Boolean
    suspend fun setEnableSessionCreation(flag: Boolean)
    suspend fun setEnabledCipherSuites(suites: Array<String>)
    suspend fun setEnabledProtocols(protocols: Array<String>)
    suspend fun setNeedClientAuth(need: Boolean)
    suspend fun setSSLParameters(params: SSLParameters)
    suspend fun setUseClientMode(mode: Boolean)
    suspend fun setWantClientAuth(want: Boolean)
}