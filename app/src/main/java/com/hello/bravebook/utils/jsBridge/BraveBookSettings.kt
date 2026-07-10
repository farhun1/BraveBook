package com.hello.bravebook.utils.jsBridge

import android.webkit.JavascriptInterface

class BraveBookSettings (
    private val toggleSettings: () -> Unit,
) {
    @JavascriptInterface
    @Suppress("unused")
    fun onSettingsToggle() = toggleSettings()
}