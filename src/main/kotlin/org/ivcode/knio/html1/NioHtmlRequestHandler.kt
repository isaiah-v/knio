package org.ivcode.knio.html1

interface NioHtmlRequestHandler {
    fun onRequest(request: NioHtmlRequest)
    fun onRequestRead()
    fun onRequestEnd(responseHandler: NioHtmlResponseHandler)
}