package org.knio.core.net.ssl

import org.knio.core.net.KServerSocket
import java.nio.channels.AsynchronousServerSocketChannel
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLServerSocket

/**
 * This class extends [KServerSocket] and provides secure server sockets using protocols such as the Secure Sockets
 * Layer (SSL) or Transport Layer Security (TLS) protocols.
 *
 * Instances of this class are generally created using an [KSSLServerSocketFactory]. The primary function of an
 * [KSSLServerSocket] is to create [KSSLSocket]s by accepting connections.
 *
 * An SSLServerSocket contains several pieces of state data which are inherited by the SSLSocket at socket creation.
 * These include the enabled cipher suites and protocols, whether client authentication is necessary, and whether
 * created sockets should begin handshaking in client or server mode. The state inherited by the created [KSSLSocket]
 * can be overridden by calling the appropriate methods.
 *
 * @see SSLServerSocket
 * @see AsynchronousServerSocketChannel
 * @see SSLEngine
 */
interface KSSLServerSocket: KServerSocket {

    override suspend fun accept(): KSSLSocket

    /**
     * Returns the list of cipher suites which are currently enabled for use by newly accepted connections.
     *
     * If this list has not been explicitly modified, a system-provided default guarantees a minimum quality of service
     * in all enabled cipher suites.
     *
     * Note that even if a suite is enabled, it may never be used. This can occur if the peer does not support it, or
     * its use is restricted, or the requisite certificates (and private keys) for the suite are not available, or an
     * anonymous suite is enabled but authentication is required.
     *
     * The returned array includes cipher suites from the list of standard cipher suite names in the JSSE Cipher Suite
     * Names section of the Java Security Standard Algorithm Names Specification, and may also include other cipher
     * suites that the provider supports.
     *
     * @return an array of cipher suite names enabled for use
     *
     * @see SSLServerSocket.getEnabledCipherSuites
     * @see SSLEngine.getEnabledCipherSuites
     */
    suspend fun getEnabledCipherSuites(): Array<String>

    /**
     * Returns the names of the protocols which are currently enabled for use by the newly accepted connections.
     *
     * Note that even if a protocol is enabled, it may never be used. This can occur if the peer does not support the
     * protocol, or its use is restricted, or there are no enabled cipher suites supported by the protocol.
     *
     * @see SSLServerSocket.getEnabledProtocols
     * @see SSLEngine.getEnabledProtocols
     */
    suspend fun getEnabledProtocols(): Array<String>

    /**
     * Returns `true` if new SSL sessions may be established by this engine.
     *
     * @return `true` indicates that sessions may be created; this is the default. false indicates that an existing
     * session must be resumed
     *
     * @see SSLServerSocket.getEnableSessionCreation
     * @see SSLEngine.getEnableSessionCreation
     */
    suspend fun getEnableSessionCreation(): Boolean


    /**
     * Returns `true` if the engine will *require* client authentication. This option is only useful to engines in the
     * server mode.
     *
     * @return `true` if client authentication is required, or `false` if no client authentication is desired.
     *
     * @see SSLServerSocket.getNeedClientAuth
     * @see SSLEngine.getNeedClientAuth
     */
    suspend fun getNeedClientAuth(): Boolean

    /**
     * Returns the [SSLParameters] in effect for this [SSLEngine]. The ciphersuites and protocols of the returned
     * [SSLParameters] are always non-null.
     *
     * @return the [SSLParameters] in effect for this [SSLEngine]
     *
     * @see SSLServerSocket.getSSLParameters
     * @see SSLEngine.getSSLParameters
     */
    suspend fun getSSLParameters(): SSLParameters

    /**
     * Returns the names of the cipher suites which could be enabled for use on this engine. Normally, only a subset of
     * these will actually be enabled by default, since this list may include cipher suites which do not meet quality
     * of service requirements for those defaults. Such cipher suites might be useful in specialized applications.
     *
     * @return an array of cipher suite names
     *
     * @see SSLServerSocket.getSupportedCipherSuites
     * @see SSLEngine.getSupportedCipherSuites
     */
    suspend fun getSupportedCipherSuites(): Array<String>

    /**
     * Returns the names of the protocols which could be enabled for use with this SSLEngine.
     *
     * @return an array of protocols supported
     *
     * @see SSLServerSocket.getSupportedProtocols
     * @see SSLEngine.getSupportedProtocols
     */
    suspend fun getSupportedProtocols(): Array<String>

    /**
     * Returns `true` if accepted connections will be in SSL client mode.
     *
     * @return `true` if the connection should use SSL client mode.
     *
     * @see SSLServerSocket.getUseClientMode
     * @see SSLEngine.getUseClientMode
     */
    suspend fun getUseClientMode(): Boolean

    /**
     * Returns `true` if client authentication will be _requested_ on newly accepted server-mode connections.
     *
     * The initial inherited setting may be overridden by calling [KSSLSocket.setNeedClientAuth] or
     * [KSSLSocket.setWantClientAuth]
     *
     * @return `true` if client authentication is requested, or `false` if no client authentication is desired.
     *
     * @see SSLServerSocket.getWantClientAuth
     * @see SSLEngine.getWantClientAuth
     */
    suspend fun getWantClientAuth(): Boolean

    /**
     * Controls whether new SSL sessions may be established by the sockets which are created from this server socket.
     *
     * [KSSLSocket]s returned from [accept] inherit this setting.
     *
     * @param flag - `true` indicates that sessions may be created; this is the default. false indicates that an
     * existing session must be resumed.
     *
     * @see SSLServerSocket.setEnableSessionCreation
     * @see SSLEngine.setEnableSessionCreation
     *
     * @todo
     *  - Test that created sockets inherit this setting
     */
    suspend fun setEnableSessionCreation(flag: Boolean)

    /**
     * Sets the cipher suites enabled for use by accepted connections.
     *
     * The cipher suites must have been listed by [getSupportedCipherSuites] as being supported. Following a successful
     * call to this method, only suites listed in the suites parameter are enabled for use.
     *
     * Suites that require authentication information which is not available in this [KServerSocket]'s authentication
     * context will not be used in any case, even if they are enabled.
     *
     * Note that the standard list of cipher suite names may be found in the JSSE Cipher Suite Names section of the
     * Java Security Standard Algorithm Names Specification. Providers may support cipher suite names not found in thi
     * list or might not use the recommended name for a certain cipher suite.
     *
     * [KSSLSocket]s returned from [accept] inherit this setting.
     *
     * @param suites - Names of all the cipher suites to enable
     *
     * @throws IllegalArgumentException when one or more of the ciphers named by the parameter is not supported, or when
     * the parameter is `null`.
     *
     * @see SSLServerSocket.setEnabledCipherSuites
     * @see SSLEngine.setEnabledCipherSuites
     *
     * @todo
     * - Test that created sockets inherit this setting
     * - Test that an exception is thrown when a cipher suite is not supported
     */
    @Throws(IllegalArgumentException::class)
    suspend fun setEnabledCipherSuites(suites: Array<String>)

    /**
     * Set the protocol versions enabled for use on this engine.
     *
     * The protocols must have been listed by [getSupportedProtocols] as being supported. Following a successful call to
     * this method, only protocols listed in the protocols parameter are enabled for use.
     *
     * @param protocols - Names of all the protocols to enable
     *
     * @throws IllegalArgumentException  when one or more of the protocols named by the parameter is not supported or
     * when the protocols parameter is `null`.
     *
     * @see SSLServerSocket.setEnabledProtocols
     * @see SSLEngine.setEnabledProtocols
     *
     * @todo
     * - Test that created sockets inherit this setting
     * - Test that an exception is thrown when a protocol is not supported
     */
    @Throws(IllegalArgumentException::class)
    suspend fun setEnabledProtocols(protocols: Array<String>)

    /**
     * Configures the engine to _require_ client authentication. This option is only useful for engines in the server
     * mode.
     *
     * An engine's client authentication setting is one of the following:
     *
     *  - client authentication required
     *  - client authentication requested
     *  - no client authentication desired
     *
     * Unlike [setWantClientAuth], if this option is set and the client chooses not to provide authentication
     * information about itself, the negotiations will stop and the engine will begin its closure procedure.
     *
     * Calling this method overrides any previous setting made by this method or [setWantClientAuth].
     *
     * @param need - set to `true` if client authentication is required, or `false` if no client authentication is
     * desired.
     *
     * @see SSLServerSocket.setNeedClientAuth
     * @see SSLEngine.setNeedClientAuth
     */
    suspend fun setNeedClientAuth(need: Boolean)

    /**
     * Applies SSLParameters to this engine.
     *
     * This means:
     *  - If [SSLParameters.getCipherSuites] is non-null, [setEnabledCipherSuites] is called with that value.
     *  - If [SSLParameters.getProtocols] is non-null, [setEnabledProtocols] is called with that value.
     *  - If [SSLParameters.getNeedClientAuth] or [SSLParameters.getWantClientAuth] return true, setNeedClientAuth(true)
     *  and setWantClientAuth(true) are called, respectively; otherwise setWantClientAuth(false) is called.
     *  - If [SSLParameters.getServerNames] is non-null, the engine will configure its server names with that value.
     *  - If [SSLParameters.getSNIMatchers] is non-null, the engine will configure its SNI matchers with that value.
     *
     * @param params - The SSLParameters to use
     *
     * @throws IllegalArgumentException if the [setEnabledCipherSuites] or the [setEnabledProtocols] call fails
     *
     * @see SSLServerSocket.setSSLParameters
     * @see SSLEngine.setSSLParameters
     */
    @Throws(IllegalArgumentException::class)
    suspend fun setSSLParameters(params: SSLParameters)

    /**
     * Configures the engine to use client (or server) mode when handshaking.
     *
     * This method must be called before any handshaking occurs. Once handshaking has begun, the mode can not be reset
     * for the life of this engine.
     *
     * Servers normally authenticate themselves, and clients are not required to do so.
     *
     * @param mode `true` if the engine should start its handshaking in `"client"` mode
     *
     * @see SSLServerSocket.setUseClientMode
     * @see SSLEngine.setUseClientMode
     */
    suspend fun setUseClientMode(mode: Boolean)

    /**
     * Configures the engine to _request_ client authentication. This option is only useful for engines in the server
     * mode.
     *
     * An engine's client authentication setting is one of the following:
     *
     *  - client authentication required
     *  - client authentication requested
     *  - no client authentication desired
     *
     * Unlike [setNeedClientAuth], if this option is set and the client chooses not to provide authentication
     * information about itself, the negotiations will continue.
     *
     * Calling this method overrides any previous setting made by this method or [setNeedClientAuth].
     *
     * @param want set to `true` if client authentication is requested, or `false` if no client authentication is
     * desired.
     *
     * @see SSLServerSocket.setWantClientAuth
     * @see SSLEngine.setWantClientAuth
     */
    suspend fun setWantClientAuth(want: Boolean)
}