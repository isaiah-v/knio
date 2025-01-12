package org.ivcode.knio.net.ssl

import org.ivcode.knio.system.ChannelFactory
import javax.net.ssl.SSLContext

// Related Extension Functions:
fun SSLContext.getKnioSSLSocketFactory(
    channelFactory: ChannelFactory = ChannelFactory.getDefault()
): KSSLSocketFactory {
    return KSSLSocketFactoryDefault(this, channelFactory)
}

fun SSLContext.getKnioSSLServerSocketFactory(
    channelFactory: ChannelFactory = ChannelFactory.getDefault()
): KSSLServerSocketFactory {
    return KSSLServerSocketFactoryDefault(this, channelFactory)
}