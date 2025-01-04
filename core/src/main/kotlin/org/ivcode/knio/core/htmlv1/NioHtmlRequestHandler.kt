package org.ivcode.knio.core.htmlv1

interface NioHtmlRequestHandler {
    fun onRequest(request: NioHtmlRequest)
    fun onRequestRead()
    fun onRequestEnd(responseHandler: NioHtmlResponseHandler)
}