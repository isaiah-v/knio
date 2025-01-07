package org.ivcode.knio.net.ssl

import org.ivcode.knio.net.KSocket
import java.io.IOException
import java.nio.channels.AsynchronousSocketChannel
import java.util.function.BiFunction
import javax.net.ssl.*


abstract class KSSLSocket (
    channel: AsynchronousSocketChannel = AsynchronousSocketChannel.open(),
    private val sslEngine: SSLEngine
): KSocket {

    fun getSupportedCipherSuites(): Array<String> {
        return sslEngine.supportedCipherSuites
    }
    fun getEnabledCipherSuites(): Array<String> {
        return sslEngine.enabledCipherSuites
    }

    fun setEnabledCipherSuites(suites: Array<String>) {
        sslEngine.enabledCipherSuites = suites
    }
    fun getSupportedProtocols(): Array<String> {
        return sslEngine.supportedProtocols
    }
    fun getEnabledProtocols(): Array<String> {
        return sslEngine.enabledProtocols
    }
    fun setEnabledProtocols(protocols: Array<String>) {
        sslEngine.enabledProtocols = protocols
    }
    fun getSession(): SSLSession {
        return sslEngine.session
    }

    fun getHandshakeSession(): SSLSession {
        return sslEngine.handshakeSession
    }

    fun addHandshakeCompletedListener(listener: HandshakeCompletedListener) {
        TODO()
    }
    fun removeHandshakeCompletedListener(listener: HandshakeCompletedListener) {
        TODO()
    }

    @Throws(IOException::class)
    suspend fun startHandshake() {
        sslEngine.beginHandshake()

        while (
            sslEngine.handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED
            && sslEngine.handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {

            when(sslEngine.handshakeStatus) {
                SSLEngineResult.HandshakeStatus.NEED_TASK -> {


                }
                SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN,
                SSLEngineResult.HandshakeStatus.NEED_WRAP -> {

                }
                SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> {

                }

                SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
                SSLEngineResult.HandshakeStatus.FINISHED -> continue
            }
        }
    }


    fun setUseClientMode(mode: Boolean) {
        sslEngine.useClientMode = mode
    }

    fun getUseClientMode(): Boolean {
        return sslEngine.useClientMode
    }
    fun setNeedClientAuth(need: Boolean) {
        sslEngine.needClientAuth = need
    }
    fun getNeedClientAuth(): Boolean {
        return sslEngine.needClientAuth
    }
    fun setWantClientAuth(want: Boolean) {
        sslEngine.wantClientAuth = want
    }
    fun getWantClientAuth(): Boolean {
        return sslEngine.wantClientAuth
    }
    fun setEnableSessionCreation(flag: Boolean) {
        sslEngine.enableSessionCreation = flag
    }

    fun getEnableSessionCreation(): Boolean {
        return sslEngine.enableSessionCreation
    }

    fun getSSLParameters(): SSLParameters {
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

    fun setSSLParameters(params: SSLParameters) {
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

    fun getApplicationProtocol(): String {
        return sslEngine.applicationProtocol
    }

    fun getHandshakeApplicationProtocol(): String {
        return sslEngine.handshakeApplicationProtocol
    }

    fun setHandshakeApplicationProtocolSelector(selector: BiFunction<KSSLSocket, MutableList<String>, String>) {
        TODO()
    }

    fun getHandshakeApplicationProtocolSelector(): BiFunction<KSSLSocket, List<String>, String> {
        TODO()
    }
}