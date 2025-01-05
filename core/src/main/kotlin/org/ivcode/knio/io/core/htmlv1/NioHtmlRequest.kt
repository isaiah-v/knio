package org.ivcode.org.ivcode.knio.io.core.htmlv1

import org.ivcode.knio.core.Reader
import java.net.SocketAddress

interface NioHtmlRequest {
    val remoteSocket: SocketAddress
    val localAddress: SocketAddress
    val method: String
    val path: String
    val version: String
    val headers: Map<String, List<String>>
    val body: Reader
}