package com.eepiemi.materialbook.utils

import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.WebViewNavigator

class ExternalRequestInterceptor(
    private val handleExternalUrl: (String) -> Unit
) : RequestInterceptor {

    override fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator
    ): WebRequestInterceptResult {

        // Brave Block List: reject ad/tracker requests before they load.
        // Additive — never blocks the main Facebook navigation (handled in
        // BraveBlockList.shouldBlock) and never un-blocks anything.
        if (BraveBlockList.shouldBlock(request.url, request.isForMainFrame)) {
            return WebRequestInterceptResult.Reject
        }

        val internalUrlRegex = Regex(
            """https?://(?!(?:l|lm)\.)[^/]*(?:facebook|messenger)\.com/.*"""
        )
        return if (internalUrlRegex.containsMatchIn(request.url) && request.isForMainFrame) {
            WebRequestInterceptResult.Allow
        } else {
            handleExternalUrl(fbRedirectSanitizer(request.url))
            WebRequestInterceptResult.Reject
        }
    }
}