package com.nexus.ai.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapWebView(
    modifier: Modifier = Modifier,
    htmlContent: String? = null,
    url: String? = null,
    javaScriptInterface: Any? = null,
    interfaceName: String = "Android",
    onPageLoaded: () -> Unit = {},
    onWebViewReady: (WebView) -> Unit = {}
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.setSupportZoom(true)
                settings.mediaPlaybackRequiresUserGesture = false

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onPageLoaded()
                    }
                }

                javaScriptInterface?.let {
                    addJavascriptInterface(it, interfaceName)
                }

                when {
                    htmlContent != null -> loadDataWithBaseURL(
                        "https://nexus.ai",
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )
                    url != null -> loadUrl(url)
                }

                onWebViewReady(this)
            }
        }
    )
}