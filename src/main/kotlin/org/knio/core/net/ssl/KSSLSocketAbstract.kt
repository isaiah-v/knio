package org.knio.core.net.ssl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.knio.core.net.KSocketAbstract
import java.nio.channels.AsynchronousSocketChannel
import java.util.function.BiFunction
import javax.net.ssl.*
import kotlin.coroutines.coroutineContext

internal abstract class KSSLSocketAbstract(
    channel: AsynchronousSocketChannel,
    protected val sslEngine: SSLEngine,
    useClientMode: Boolean,
): KSSLSocket, KSocketAbstract(channel) {

    private var handshakeListenersMutex:Mutex? = Mutex()
    private var handshakeListeners: MutableSet<KHandshakeCompletedListener>? = mutableSetOf()

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
        startHandshake()
        return sslEngine.session
    }

    override suspend fun getHandshakeSession(): SSLSession? {
        return sslEngine.handshakeSession
    }

    override suspend fun addHandshakeCompletedListener(listener: KHandshakeCompletedListener) {
        // per the java implementation, ignore the listener if the handshake is already complete
        handshakeListenersMutex?.withLock {
            handshakeListeners?.add(listener)
        }
    }

    override suspend fun removeHandshakeCompletedListener(listener: KHandshakeCompletedListener) {
        handshakeListenersMutex?.withLock {
            handshakeListeners?.remove(listener)
        }
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

    override suspend fun getApplicationProtocol(): String {
        return sslEngine.applicationProtocol
    }

    override suspend fun getHandshakeApplicationProtocol(): String {
        return sslEngine.handshakeApplicationProtocol
    }

    override suspend fun setHandshakeApplicationProtocolSelector(selector: BiFunction<KSSLSocket, List<String>, String?>) {
        sslEngine.handshakeApplicationProtocolSelector = HandshakeApplicationProtocolSelector(selector)
    }

    override suspend fun getHandshakeApplicationProtocolSelector(): BiFunction<KSSLSocket, List<String>, String?>? {
        return sslEngine.handshakeApplicationProtocolSelector?.let {
            if(it is HandshakeApplicationProtocolSelector) {
                it.selector
            } else {
                null
            }
        }
    }

    final override suspend fun startHandshake() {
        try {
            doHandshake()
            onSuccessfulHandshake()
        } catch (th: Throwable) {
            close()
            throw th
        }
    }

    /**
     * Run all the handshake listeners. Runs as its own job.
     */
    private suspend fun onSuccessfulHandshake() {
        // get the listeners and close it so no more can be added
        val listeners = handshakeListenersMutex?.withLock {
            val listener = handshakeListeners

            handshakeListeners = null
            handshakeListenersMutex = null

            listener
        }

        if(listeners.isNullOrEmpty()) {
            return
        }

        CoroutineScope(coroutineContext).launch {
            listeners.forEach {
                runHandshakeCompletedListener(it)
            }
        }
    }

    private suspend fun runHandshakeCompletedListener(listener: KHandshakeCompletedListener) {
        try {
            listener.handshakeCompleted(
                KHandshakeCompletedEvent(
                    this,
                    sslEngine.session
                )
            )
        } catch (th: Throwable) {
            // nothing left to do. print the stack trace and move on
            th.printStackTrace()
        }
    }

    protected abstract suspend fun doHandshake()

    /**
     * A wrapper for the application protocol selector function to work with the SSLEngine.
     *
     * @see SSLEngine.getHandshakeApplicationProtocolSelector
     * @see SSLEngine.setHandshakeApplicationProtocolSelector
     */
    private class HandshakeApplicationProtocolSelector (
        val selector: BiFunction<KSSLSocket, List<String>, String?>
    ): BiFunction<SSLEngine, List<String>, String?> {
        override fun apply(t: SSLEngine, u: List<String>): String? {
            return selector.apply(t as KSSLSocket, u)
        }
    }
}