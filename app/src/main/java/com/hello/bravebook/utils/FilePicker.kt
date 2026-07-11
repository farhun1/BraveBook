package com.hello.bravebook.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.hello.bravebook.utils.FullscreenManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.multiplatform.webview.web.AccompanistWebChromeClient
import com.multiplatform.webview.web.PlatformWebViewParams

// src: https://github.com/KevinnZou/compose-webview-multiplatform

@Composable
fun fileChooserWebViewParams(
    fullscreenManager: FullscreenManager,
    onFullscreenView: (View?) -> Unit,
): PlatformWebViewParams {
    var fileChooserIntent by remember { mutableStateOf<Intent?>(null) }

    val webViewChromeClient =
        remember {
            FileChoosableWebChromeClient(
                onShowFilePicker = { fileChooserIntent = it },
                fullscreenManager = fullscreenManager,
                onFullscreenView = onFullscreenView,
            )
        }

    val webViewClient = remember { BlockingWebViewClient() }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK) {
                webViewChromeClient.cancelFileChooser()
                return@rememberLauncherForActivityResult
            }

            val intent = result.data
            if (intent == null) {
                webViewChromeClient.cancelFileChooser()
                return@rememberLauncherForActivityResult
            }

            val singleFile: Uri? = intent.data
            val multiFiles: List<Uri>? = intent.getUris()

            when {
                singleFile != null -> webViewChromeClient.onReceiveFiles(arrayOf(singleFile))
                multiFiles != null -> webViewChromeClient.onReceiveFiles(multiFiles.toTypedArray())
                else -> {
                    webViewChromeClient.cancelFileChooser()
                }
            }
        }

    LaunchedEffect(key1 = fileChooserIntent) {
        fileChooserIntent?.let {
            try {
                launcher.launch(fileChooserIntent!!)
            } catch (_: ActivityNotFoundException) {
                webViewChromeClient.cancelFileChooser()
            }
        }
    }

    return PlatformWebViewParams(client = webViewClient, chromeClient = webViewChromeClient)
}

private fun Intent.getUris(): List<Uri>? {
    val clipData = clipData ?: return null
    return (0 until clipData.itemCount).map { clipData.getItemAt(it).uri }
}

private class FileChoosableWebChromeClient(
    private val onShowFilePicker: (Intent) -> Unit,
    private val fullscreenManager: FullscreenManager,
    private val onFullscreenView: (View?) -> Unit,
) : AccompanistWebChromeClient() {
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?,
    ): Boolean {
        this.filePathCallback = filePathCallback
        val filePickerIntent = fileChooserParams?.createIntent()

        if (filePickerIntent == null) cancelFileChooser()
        else onShowFilePicker(filePickerIntent)

        return true
    }

    fun onReceiveFiles(uris: Array<Uri>) {
        filePathCallback?.onReceiveValue(uris)
        filePathCallback = null
    }

    fun cancelFileChooser() {
        filePathCallback?.onReceiveValue(null)
        filePathCallback = null
    }

    // --- Fullscreen video support (Bug 1) ---
    override fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        if (view == null) return
        fullscreenManager.enter(view, callback ?: return)
        onFullscreenView(view)
    }

    override fun onHideCustomView() {
        if (!fullscreenManager.isFullscreen) return
        val cb = fullscreenManager.exit()
        onFullscreenView(null)
        (cb as? WebChromeClient.CustomViewCallback)?.onCustomViewHidden()
    }
}
