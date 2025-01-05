package org.ivcode.org.ivcode.knio.io.core.htmlv1

interface NioHtmlRequestHandler {
    fun onRequest(request: NioHtmlRequest)
    fun onRequestRead()
    fun onRequestEnd(responseHandler: NioHtmlResponseHandler)
}