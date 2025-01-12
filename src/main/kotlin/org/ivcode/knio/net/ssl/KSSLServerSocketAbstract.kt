package org.ivcode.knio.net.ssl

import org.jetbrains.annotations.Blocking
import java.net.InetAddress
import java.net.SocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLParameters

@Blocking
internal abstract class KSSLServerSocketAbstract(
    private val sslContext: SSLContext,
    protected val serverChannel: AsynchronousServerSocketChannel
): KSSLServerSocket {

    protected var isEnableSessionCreation: Boolean = true
    protected var isUseClientMode: Boolean = false

    protected suspend fun createSSLEngine(): SSLEngine {
        val engine = sslContext.createSSLEngine()

        engine.enableSessionCreation = isEnableSessionCreation
        engine.useClientMode = isUseClientMode

        return engine
    }

    override suspend fun getEnabledCipherSuites(): Array<String> {
        return sslContext.defaultSSLParameters.cipherSuites
    }

    override suspend fun getEnabledProtocols(): Array<String> {
        return sslContext.defaultSSLParameters.protocols
    }

    override suspend fun getEnableSessionCreation(): Boolean {
        return isEnableSessionCreation
    }

    override suspend fun getNeedClientAuth(): Boolean {
        return sslContext.defaultSSLParameters.needClientAuth
    }

    override suspend fun getSSLParameters(): SSLParameters {
        return sslContext.defaultSSLParameters
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
        return sslContext.defaultSSLParameters.wantClientAuth
    }

    override suspend fun setEnableSessionCreation(flag: Boolean) {
        isEnableSessionCreation = flag
    }

    override suspend fun setEnabledCipherSuites(suites: Array<String>) {
        sslContext.defaultSSLParameters.cipherSuites = suites
    }

    override suspend fun setEnabledProtocols(protocols: Array<String>) {
        sslContext.defaultSSLParameters.protocols = protocols
    }

    override suspend fun setNeedClientAuth(need: Boolean) {
        sslContext.defaultSSLParameters.needClientAuth = need
    }

    override suspend fun setSSLParameters(params: SSLParameters) {
        sslContext.defaultSSLParameters.algorithmConstraints = params.algorithmConstraints
        sslContext.defaultSSLParameters.applicationProtocols = params.applicationProtocols
        sslContext.defaultSSLParameters.cipherSuites = params.cipherSuites
        sslContext.defaultSSLParameters.endpointIdentificationAlgorithm = params.endpointIdentificationAlgorithm
        sslContext.defaultSSLParameters.maximumPacketSize = params.maximumPacketSize
        sslContext.defaultSSLParameters.needClientAuth = params.needClientAuth
        sslContext.defaultSSLParameters.protocols = params.protocols
        sslContext.defaultSSLParameters.serverNames = params.serverNames
        sslContext.defaultSSLParameters.useCipherSuitesOrder = params.useCipherSuitesOrder
        sslContext.defaultSSLParameters.wantClientAuth = params.wantClientAuth
    }

    override suspend fun setUseClientMode(mode: Boolean) {
        isUseClientMode = mode
    }

    override suspend fun setWantClientAuth(want: Boolean) {
        sslContext.defaultSSLParameters.wantClientAuth = want
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