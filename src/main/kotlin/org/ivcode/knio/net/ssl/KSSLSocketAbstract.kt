package org.ivcode.knio.net.ssl

import org.ivcode.knio.net.KSocketAbstract
import java.nio.channels.AsynchronousSocketChannel
import javax.net.ssl.HandshakeCompletedListener
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSession

internal abstract class KSSLSocketAbstract(
    channel: AsynchronousSocketChannel,
    protected val sslEngine: SSLEngine,
    useClientMode: Boolean,
): KSSLSocket, KSocketAbstract(channel) {

    init {
        sslEngine.useClientMode = useClientMode
    }

    override suspend fun getSupportedCipherSuites(): Array<String> {
        return sslEngine.supportedCipherSuites
    }
    override suspend fun getEnabledCipherSuites(): Array<String> {
        return sslEngine.enabledCipherSuites
    }

    override suspend fun setEnabledCipherSuites(suites: Array<String>) {
        sslEngine.enabledCipherSuites = suites
    }
    override suspend fun getSupportedProtocols(): Array<String> {
        return sslEngine.supportedProtocols
    }
    override suspend fun getEnabledProtocols(): Array<String> {
        return sslEngine.enabledProtocols
    }
    override suspend fun setEnabledProtocols(protocols: Array<String>) {
        sslEngine.enabledProtocols = protocols
    }
    override suspend fun getSession(): SSLSession {
        return sslEngine.session
    }

    override suspend fun getHandshakeSession(): SSLSession {
        return sslEngine.handshakeSession
    }

    override suspend fun addHandshakeCompletedListener(listener: HandshakeCompletedListener) {
        TODO()
    }
    override suspend fun removeHandshakeCompletedListener(listener: HandshakeCompletedListener) {
        TODO()
    }

    override suspend fun setUseClientMode(mode: Boolean) {
        sslEngine.useClientMode = mode
    }

    override suspend fun getUseClientMode(): Boolean {
        return sslEngine.useClientMode
    }
    override suspend fun setNeedClientAuth(need: Boolean) {
        sslEngine.needClientAuth = need
    }
    override suspend fun getNeedClientAuth(): Boolean {
        return sslEngine.needClientAuth
    }
    override suspend fun setWantClientAuth(want: Boolean) {
        sslEngine.wantClientAuth = want
    }
    override suspend fun getWantClientAuth(): Boolean {
        return sslEngine.wantClientAuth
    }
    override suspend fun setEnableSessionCreation(flag: Boolean) {
        sslEngine.enableSessionCreation = flag
    }

    override suspend fun getEnableSessionCreation(): Boolean {
        return sslEngine.enableSessionCreation
    }

    override suspend fun getSSLParameters(): SSLParameters {
        val params = SSLParameters()
        params.cipherSuites = getEnabledCipherSuites()
        params.protocols = getEnabledProtocols()
        if (getNeedClientAuth()) {
            params.needClientAuth = true
        } else if (getWantClientAuth()) {
            params.wantClientAuth = true
        }
        return params
    }

    override suspend fun setSSLParameters(params: SSLParameters) {
        var s = params.cipherSuites
        if (s != null) {
            setEnabledCipherSuites(s)
        }
        s = params.protocols
        if (s != null) {
            setEnabledProtocols(s)
        }
        if (params.needClientAuth) {
            setNeedClientAuth(true)
        } else {
            setWantClientAuth(params.wantClientAuth)
        }
    }

    override suspend fun getApplicationProtocol(): String {
        return sslEngine.applicationProtocol
    }

    override suspend fun getHandshakeApplicationProtocol(): String {
        return sslEngine.handshakeApplicationProtocol
    }

    override suspend fun setHandshakeApplicationProtocolSelector(selector: (KSSLSocket, MutableList<String>) -> String?) {
        TODO("Not yet implemented")
    }

    override suspend fun getHandshakeApplicationProtocolSelector(): ((KSSLSocket, MutableList<String>) -> String?)? {
        TODO("Not yet implemented")
    }


}