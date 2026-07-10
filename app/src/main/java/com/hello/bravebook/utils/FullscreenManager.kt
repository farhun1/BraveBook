package com.hello.bravebook.utils

/**
 * Android-free holder for WebView fullscreen-video state.
 *
 * `view`/`callback` are typed as [Any] (not [android.view.View] /
 * [android.webkit.WebChromeClient.CustomViewCallback]) so this class can be
 * unit-tested on the JVM without a device. The WebChromeClient casts on use.
 */
class FullscreenManager {
    var current: Any? = null
        private set
    var callback: Any? = null
        private set
    var isFullscreen: Boolean = false
        private set

    fun enter(view: Any, callback: Any) {
        this.current = view
        this.callback = callback
        this.isFullscreen = true
    }

    /** Clears state and returns the stored CustomViewCallback (or null). */
    fun exit(): Any? {
        val cb = callback
        current = null
        callback = null
        isFullscreen = false
        return cb
    }
}
