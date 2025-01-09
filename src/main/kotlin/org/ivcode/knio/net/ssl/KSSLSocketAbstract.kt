package org.ivcode.knio.net.ssl

import org.ivcode.org.ivcode.knio.net.KSocketAbstract
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

    override fun getSupportedCipherSuites(): Array<String> {
        return sslEngine.supportedCipherSuites
    }
    override fun getEnabledCipherSuites(): Array<String> {
        return sslEngine.enabledCipherSuites
    }

    override fun setEnabledCipherSuites(suites: Array<String>) {
        sslEngine.enabledCipherSuites = suites
    }
    fun getSupportedProtocols(): Array<String> {
        return sslEngine.supportedProtocols
    }
    override fun getEnabledProtocols(): Array<String> {
        return sslEngine.enabledProtocols
    }
    override fun setEnabledProtocols(protocols: Array<String>) {
        sslEngine.enabledProtocols = protocols
    }
    override fun getSession(): SSLSession {
        return sslEngine.session
    }

    override fun getHandshakeSession(): SSLSession {
        return sslEngine.handshakeSession
    }

    override fun addHandshakeCompletedListener(listener: HandshakeCompletedListener) {
        TODO()
    }
    override fun removeHandshakeCompletedListener(listener: HandshakeCompletedListener) {
        TODO()
    }

    override fun setUseClientMode(mode: Boolean) {
        sslEngine.useClientMode = mode
    }

    override fun getUseClientMode(): Boolean {
        return sslEngine.useClientMode
    }
    override fun setNeedClientAuth(need: Boolean) {
        sslEngine.needClientAuth = need
    }
    override fun getNeedClientAuth(): Boolean {
        return sslEngine.needClientAuth
    }
    override fun setWantClientAuth(want: Boolean) {
        sslEngine.wantClientAuth = want
    }
    override fun getWantClientAuth(): Boolean {
        return sslEngine.wantClientAuth
    }
    override fun setEnableSessionCreation(flag: Boolean) {
        sslEngine.enableSessionCreation = flag
    }

    override fun getEnableSessionCreation(): Boolean {
        return sslEngine.enableSessionCreation
    }

    override fun getSSLParameters(): SSLParameters {
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

    override fun setSSLParameters(params: SSLParameters) {
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

    override fun getApplicationProtocol(): String {
        return sslEngine.applicationProtocol
    }

    override fun getHandshakeApplicationProtocol(): String {
        return sslEngine.handshakeApplicationProtocol
    }

    override fun setHandshakeApplicationProtocolSelector(selector: (KSSLSocket, MutableList<String>) -> String?) {
        TODO("Not yet implemented")
    }

    override fun getHandshakeApplicationProtocolSelector(): ((KSSLSocket, MutableList<String>) -> String?)? {
        TODO("Not yet implemented")
    }


}