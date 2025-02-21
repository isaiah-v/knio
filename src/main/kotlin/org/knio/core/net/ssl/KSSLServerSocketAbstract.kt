package org.knio.core.net.ssl

import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLParameters

internal abstract class KSSLServerSocketAbstract(
    private val sslContext: SSLContext,
    protected val serverChannel: AsynchronousServerSocketChannel
): KSSLServerSocket {

    private val sslParameters: SSLParameters = sslContext.defaultSSLParameters

    private var isEnableSessionCreation: Boolean = true
    private var isUseClientMode: Boolean = false

    protected suspend fun createSSLEngine(): SSLEngine {
        val engine = sslContext.createSSLEngine()

        // note: The documentation states that an SNIMatcher is set. No logic exists in JDK 21
        engine.sslParameters = sslParameters

        engine.enableSessionCreation = isEnableSessionCreation
        engine.useClientMode = isUseClientMode

        return engine
    }

    override suspend fun getEnabledCipherSuites(): Array<String> {
        return sslParameters.cipherSuites
    }

    override suspend fun getEnabledProtocols(): Array<String> {
        return sslParameters.protocols
    }

    override suspend fun getEnableSessionCreation(): Boolean {
        return isEnableSessionCreation
    }

    override suspend fun getNeedClientAuth(): Boolean {
        return sslParameters.needClientAuth
    }

    override suspend fun getSSLParameters(): SSLParameters {
        return sslParameters
    }

    override suspend fun getSupportedCipherSuites(): Array<String> {
        return sslContext.supportedSSLParameters.cipherSuites
    }

    override suspend fun getSupportedProtocols(): Array<String> {
        return sslContext.supportedSSLParameters.protocols
    }

    override suspend fun getUseClientMode(): Boolean {
        return isUseClientMode
    }

    override suspend fun getWantClientAuth(): Boolean {
        return sslParameters.wantClientAuth
    }

    override suspend fun setEnableSessionCreation(flag: Boolean) {
        isEnableSessionCreation = flag
    }

    override suspend fun setEnabledCipherSuites(suites: Array<String>) {
        sslParameters.cipherSuites = suites
    }

    override suspend fun setEnabledProtocols(protocols: Array<String>) {
        sslParameters.protocols = protocols
    }

    override suspend fun setNeedClientAuth(need: Boolean) {
        sslParameters.needClientAuth = need
    }

    override suspend fun setSSLParameters(params: SSLParameters) {
        if(params.cipherSuites != null) {
            sslParameters.cipherSuites = params.cipherSuites
        }

        if(params.protocols != null) {
            sslParameters.protocols = params.protocols
        }

        if (params.needClientAuth) {
            sslParameters.needClientAuth = true
        } else {
            sslParameters.wantClientAuth = params.wantClientAuth
        }

        if (params.serverNames != null) {
            sslParameters.serverNames = params.serverNames
        }

        if (params.sniMatchers != null) {
            sslParameters.sniMatchers = params.sniMatchers
        }
    }

    override suspend fun setUseClientMode(mode: Boolean) {
        isUseClientMode = mode
    }

    override suspend fun setWantClientAuth(want: Boolean) {
        sslParameters.wantClientAuth = want
    }

    override suspend fun getInetAddress(): InetAddress? {
        val address = serverChannel.localAddress ?: return null
        return if(address is java.net.InetSocketAddress) {
            address.address
        } else {
            null
        }
    }

    override suspend fun getLocalPort(): Int {
        val address = serverChannel.localAddress ?: return -1
        return if(address is java.net.InetSocketAddress) {
            address.port
        } else {
            -1
        }
    }

    override suspend fun getLocalSocketAddress(): SocketAddress? {
        return serverChannel.localAddress
    }

    override suspend fun setReuseAddress(on: Boolean) {
        @Suppress("BlockingMethodInNonBlockingContext")
        serverChannel.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, on)
    }

    override suspend fun getReuseAddress(): Boolean {
        @Suppress("BlockingMethodInNonBlockingContext")
        return serverChannel.getOption(java.net.StandardSocketOptions.SO_REUSEADDR)
    }

    override suspend fun setReceiveBufferSize(size: Int) {
        @Suppress("BlockingMethodInNonBlockingContext")
        serverChannel.setOption(java.net.StandardSocketOptions.SO_RCVBUF, size)
    }

    override suspend fun getReceiveBufferSize(): Int {
        @Suppress("BlockingMethodInNonBlockingContext")
        return serverChannel.getOption(java.net.StandardSocketOptions.SO_RCVBUF)
    }

    override suspend fun isBound(): Boolean {
        return serverChannel.localAddress != null
    }

    override suspend fun isClosed(): Boolean {
        return !serverChannel.isOpen
    }
}