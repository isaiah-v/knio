package org.ivcode.knio.net.ssl

import org.ivcode.org.ivcode.knio.system.ChannelFactory
import javax.net.ssl.SSLContext

// Related Extension Functions:
fun SSLContext.getKnioSSLSocketFactory(
    channelFactory: ChannelFactory = ChannelFactory.getDefault()
): KSSLSocketFactory {
    return KSSLSocketFactoryDefault(this, channelFactory)
}